/**
 * 用 Edge DevTools Protocol 做一次用户端页面冒烟测试：
 *  - 调后端登录接口拿 token
 *  - 写入 localStorage 并整页导航（模拟刷新后恢复会话）
 *  - 检查学生成绩页、教师受理页是否渲染出数据
 *  - 保存截图到 test-artifacts
 *
 * 前置：Edge 以 --remote-debugging-port=9222 启动；user-front 运行在 5173；后端 8080。
 */
import fs from 'node:fs'
import path from 'node:path'

const CDP = 'http://127.0.0.1:9222'
const APP = 'http://localhost:5173'
const OUT = path.resolve('test-artifacts')
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
    width: 1440,
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

  async function goto(url) {
    const [base, hash] = url.split('#')
    const sep = base.includes('?') ? '&' : '?'
    const finalUrl = base + sep + 'r=' + Date.now() + (hash ? '#' + hash : '')
    await send('Page.navigate', { url: finalUrl })
    await sleep(2600)
  }

  async function shot(name) {
    const r = await send('Page.captureScreenshot', { format: 'png' })
    const file = path.join(OUT, name)
    fs.writeFileSync(file, Buffer.from(r.data, 'base64'))
    console.log('screenshot:', file)
  }

  async function loginAndStore(username, password) {
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
      return j.data.user;
    })()`)
  }

  // 先进入应用页面，保证 fetch 的页面 origin 是 localhost:5173
  await goto(APP + '/#/login')

  // 学生
  const student = await loginAndStore('2021001', '123456')
  console.log('student identity:', student)
  await goto(APP + '/#/student/scores')
  await sleep(1200)
  const studentText = await evaluate('document.body.innerText')
  console.log('student hash:', await evaluate('location.hash'))
  console.log('student has 成绩查询:', studentText.includes('成绩查询'))
  console.log('student has 语文:', studentText.includes('语文'))
  console.log('student has 查分中:', studentText.includes('查分中'))
  await shot('ui-student-scores.png')

  // 教师
  const teacher = await loginAndStore('T001', '123456')
  console.log('teacher identity:', teacher)
  await goto(APP + '/#/teacher/appeals')
  await sleep(1200)
  const teacherText = await evaluate('document.body.innerText')
  console.log('teacher hash:', await evaluate('location.hash'))
  console.log('teacher has 受理查分申请:', teacherText.includes('受理查分申请'))
  console.log('teacher has 待受理:', teacherText.includes('待受理'))
  await shot('ui-teacher-appeals.png')

  // 教师录入页
  await goto(APP + '/#/teacher/scores/add')
  await sleep(1600)
  const addText = await evaluate('document.body.innerText')
  console.log('add has 添加学生成绩:', addText.includes('添加学生成绩'))
  console.log('add has 高一(1)班:', addText.includes('高一(1)班'))
  await shot('ui-teacher-add.png')

  // 教师修改页
  await goto(APP + '/#/teacher/scores/edit')
  await sleep(1200)
  const editText = await evaluate('document.body.innerText')
  console.log('edit has 修改学生成绩:', editText.includes('修改学生成绩'))
  await shot('ui-teacher-edit.png')
}

main().then(
  () => process.exit(0),
  (error) => {
    console.error('UI smoke failed:', error)
    process.exit(1)
  }
)
