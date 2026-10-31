/**
 * 验证新增的「预测成绩 / 成绩趋势 / 个人中心」页面。
 * 前置：Edge --remote-debugging-port=9222；后端 8080；user-front 5173。
 */
import fs from 'node:fs'
import path from 'node:path'

const CDP = 'http://127.0.0.1:9222'
const APP = 'http://localhost:5173'
const OUT = path.resolve('report/assets')
const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

async function targetWs() {
  for (let i = 0; i < 30; i++) {
    try {
      const list = await (await fetch(CDP + '/json')).json()
      const page = list.find((t) => t.type === 'page')
      if (page) return page.webSocketDebuggerUrl
    } catch (error) {
      // Edge 启动中
    }
    await sleep(500)
  }
  throw new Error('未找到 Edge CDP 页面')
}

function connect(url) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(url)
    const pending = new Map()
    let seq = 0
    ws.onmessage = (event) => {
      const msg = JSON.parse(event.data)
      if (msg.id && pending.has(msg.id)) {
        const { resolve: ok, reject: fail } = pending.get(msg.id)
        pending.delete(msg.id)
        if (msg.error) fail(new Error(JSON.stringify(msg.error)))
        else ok(msg.result)
      }
    }
    ws.onerror = (e) => reject(e)
    ws.onopen = () =>
      resolve({
        send(method, params = {}) {
          return new Promise((ok, fail) => {
            const id = ++seq
            pending.set(id, { resolve: ok, reject: fail })
            ws.send(JSON.stringify({ id, method, params }))
          })
        }
      })
  })
}

async function main() {
  fs.mkdirSync(OUT, { recursive: true })
  const { send } = await connect(await targetWs())
  await send('Page.enable')
  await send('Runtime.enable')
  await send('Emulation.setDeviceMetricsOverride', {
    width: 1440,
    height: 1000,
    deviceScaleFactor: 1,
    mobile: false
  })

  async function evaluate(expression) {
    const r = await send('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true })
    if (r.exceptionDetails) throw new Error(r.exceptionDetails.text)
    return r.result.value
  }

  async function goto(hash) {
    await send('Page.navigate', { url: APP + '/?r=' + Date.now() + hash })
    await sleep(2800)
  }

  async function shot(name) {
    const r = await send('Page.captureScreenshot', { format: 'png' })
    fs.writeFileSync(path.join(OUT, name), Buffer.from(r.data, 'base64'))
    console.log('screenshot:', name)
  }

  async function login(username, password) {
    return evaluate(`(async () => {
      localStorage.clear();
      const r = await fetch('/api/portal/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username: '${username}', password: '${password}'})
      });
      const j = await r.json();
      if (j.code !== 200) throw new Error(j.msg || 'login failed');
      localStorage.setItem('grade-system:token', j.data.token);
      localStorage.setItem('grade-system:user', JSON.stringify(j.data.user));
      return j.data.user.role;
    })()`)
  }

  await goto('#/login')

  console.log('student role:', await login('2021001', '123456'))
  await goto('#/student/scores')
  await sleep(1200)
  let text = await evaluate('document.body.innerText')
  console.log('预测成绩卡片:', text.includes('预测下次考试成绩'))
  console.log('趋势图标题:', text.includes('最近成绩起伏'))
  console.log('平均分参考:', text.includes('平均'))
  await shot('ui-student-predict.png')

  await goto('#/profile')
  await sleep(800)
  text = await evaluate('document.body.innerText')
  console.log('个人中心:', text.includes('个人中心'))
  console.log('学习概览:', text.includes('学习概览'))
  console.log('小贴士:', text.includes('小贴士'))
  await shot('ui-student-profile.png')

  console.log('teacher role:', await login('T001', '123456'))
  await goto('#/profile')
  await sleep(800)
  text = await evaluate('document.body.innerText')
  console.log('教学概览:', text.includes('教学概览'))
  console.log('管理班级:', text.includes('管理班级'))
  await shot('ui-teacher-profile.png')
}

main().then(
  () => process.exit(0),
  (error) => {
    console.error('profile smoke failed:', error)
    process.exit(1)
  }
)
