/**
 * 管理端页面冒烟测试：
 *  - 通过 /dev-api/login 拿 admin token，写入 Admin-Token Cookie
 *  - 整页刷新后进入 /index，检查「成绩管理」菜单
 *  - 进入 /grade/score、/grade/appeal，检查生成页面是否渲染数据
 *  - 保存截图到 report/assets
 *
 * 前置：Edge 以 --remote-debugging-port=9222 启动；管理端 8081；后端 8080。
 */
import fs from 'node:fs'
import path from 'node:path'

const CDP = 'http://127.0.0.1:9222'
const APP = 'http://localhost:8081'
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
        ws,
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
    width: 1600,
    height: 900,
    deviceScaleFactor: 1,
    mobile: false
  })

  async function evaluate(expression) {
    const r = await send('Runtime.evaluate', {
      expression,
      awaitPromise: true,
      returnByValue: true
    })
    if (r.exceptionDetails) throw new Error(r.exceptionDetails.text)
    return r.result.value
  }

  async function goto(pathname) {
    const sep = pathname.includes('?') ? '&' : '?'
    await send('Page.navigate', { url: APP + pathname + sep + 'r=' + Date.now() })
    await sleep(3000)
  }

  async function shot(name) {
    const r = await send('Page.captureScreenshot', { format: 'png' })
    const file = path.join(OUT, name)
    fs.writeFileSync(file, Buffer.from(r.data, 'base64'))
    console.log('screenshot:', file)
  }

  await goto('/login')
  const login = await evaluate(`(async () => {
    const r = await fetch('/dev-api/login', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({username: 'admin', password: 'admin123'})
    });
    const j = await r.json();
    if (j.code !== 200) throw new Error(j.msg || 'admin login failed');
    document.cookie = 'Admin-Token=' + j.token + '; path=/';
    return {code: j.code, tokenLength: (j.token || '').length};
  })()`)
  console.log('admin login:', login)

  await goto('/index')
  let text = await evaluate('document.body.innerText')
  console.log('index url:', await evaluate('location.href'))
  console.log('index has 成绩管理:', text.includes('成绩管理'))
  console.log('index has 学生管理:', text.includes('学生管理'))
  await shot('ui-admin-index.png')

  await goto('/grade/score')
  await sleep(1500)
  text = await evaluate('document.body.innerText')
  console.log('score url:', await evaluate('location.href'))
  console.log('score has 成绩编号:', text.includes('成绩编号'))
  console.log('score has SC2025:', text.includes('SC2025'))
  await shot('ui-admin-score.png')

  await goto('/grade/appeal')
  await sleep(1500)
  text = await evaluate('document.body.innerText')
  console.log('appeal has 查分申请:', text.includes('查分申请'))
  console.log('appeal has AP2025:', text.includes('AP2025'))
  await shot('ui-admin-appeal.png')

  await goto('/grade/student')
  await sleep(1500)
  text = await evaluate('document.body.innerText')
  console.log('student has 学号:', text.includes('学号'))
  await shot('ui-admin-student.png')
}

main().then(
  () => process.exit(0),
  (error) => {
    console.error('Admin UI smoke failed:', error)
    process.exit(1)
  }
)
