<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { api } from '@/api/request'
import { useScoreStore } from '@/stores/score'
import { useUserStore } from '@/stores/user'
import { calcAvgGpa, calcAvgScore } from '@/utils/score'

/** 右下角 AI 咨询：蓝色圆形按钮 + 右侧对话抽屉（DeepSeek 代理） */

const user = useUserStore()
const data = useScoreStore()

const STORAGE_KEY = 'grade-system:ai-chat'
const VISITED_KEY = 'grade-system:ai-visited'

const open = ref(false)
const sending = ref(false)
const input = ref('')
const messages = ref([])
const listRef = ref(null)
const inputRef = ref(null)
const enabled = ref(true)
const visited = ref(localStorage.getItem(VISITED_KEY) === '1')

const QUICK_PROMPTS = [
  '帮我分析一下我的成绩',
  '查分申请怎么处理？',
  '怎样提高薄弱科目？'
]

function loadHistory() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const list = JSON.parse(raw)
      if (Array.isArray(list)) return list.slice(-40)
    }
  } catch (error) {
    console.warn('读取 AI 对话记录失败', error)
  }
  return []
}

messages.value = loadHistory()

const showDot = computed(() => !visited.value && !open.value)

function persist() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messages.value.slice(-40)))
  } catch (error) {
    console.warn('保存 AI 对话记录失败', error)
  }
}

watch(messages, persist, { deep: true })

function now() {
  const d = new Date()
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

/** 轻量 Markdown：先转义 HTML，再处理加粗、列表与换行，避免 XSS */
function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function renderMarkdown(text) {
  let html = escapeHtml(text)
  html = html.replace(/^#{1,6}\s*(.+)$/gm, '<strong>$1</strong>')
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/^\s*[-*]\s+(.+)$/gm, '<span class="md-li">• $1</span>')
  html = html.replace(/\n/g, '<br>')
  return html
}

function scrollToBottom() {
  nextTick(() => {
    const el = listRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

watch(open, async (value) => {
  if (value) {
    visited.value = true
    localStorage.setItem(VISITED_KEY, '1')
    await checkStatus()
    scrollToBottom()
    nextTick(() => inputRef.value?.focus())
  }
})

async function checkStatus() {
  try {
    const status = await api.aiStatus()
    enabled.value = Boolean(status?.enabled)
  } catch (error) {
    // 状态查询失败不阻塞聊天，真正发送时再提示
    enabled.value = true
  }
}

/** 给 AI 的上下文：当前身份 + 成绩概况，让回答更贴合本系统 */
function buildSystemPrompt() {
  const lines = [
    '你是「成绩管理系统」的 AI 学习助手，请用中文、简洁、友好地回答。',
    '系统角色：学生可查成绩、提交查分申请；教师可录入/修改成绩、受理查分。'
  ]
  if (user.isStudent) {
    lines.push(`当前用户：学生 ${user.name}，学号 ${user.loginId}，班级 ${user.className || '—'}。`)
    const rows = data.scoreRowsOfStudent(user.loginId, {})
    if (rows.length) {
      const avg = calcAvgScore(rows)
      const gpa = calcAvgGpa(rows)
      const detail = rows
        .map((row) => `${row.courseName}${row.totalScore}`)
        .join('、')
      lines.push(`已修 ${rows.length} 门，平均分 ${avg}，平均绩点 ${gpa}。`)
      lines.push(`成绩明细（课程+总评）：${detail}。`)
    }
    const pending = data.appeals.filter(
      (item) => item.studentId === user.loginId && item.status === 'PENDING'
    ).length
    lines.push(`当前待受理查分申请 ${pending} 条。`)
  } else if (user.isTeacher) {
    const classText = user.manageClassIds?.length ? user.manageClassIds.join('、') : '—'
    const pending = data.appeals.filter((item) => item.status === 'PENDING').length
    lines.push(`当前用户：教师 ${user.name}，工号 ${user.loginId}，管理班级 ${classText}。`)
    lines.push(`当前待受理查分申请 ${pending} 条。`)
  }
  lines.push('涉及查分流程时按此顺序说明：学生提交申请 → 教师受理（维持原成绩或更正）→ 结果留痕。')
  return lines.join('\n')
}

async function send(text) {
  const content = String(text ?? input.value).trim()
  if (!content || sending.value) return

  messages.value.push({ role: 'user', content, time: now() })
  input.value = ''
  sending.value = true
  scrollToBottom()

  const history = messages.value
    .filter((item) => item.role === 'user' || item.role === 'assistant')
    .slice(-10)
    .map((item) => ({ role: item.role, content: item.content }))

  try {
    const result = await api.aiChat({
      messages: [{ role: 'system', content: buildSystemPrompt() }, ...history]
    })
    messages.value.push({ role: 'assistant', content: result.reply, time: now() })
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: error.message || 'AI 服务暂时不可用，请稍后再试',
      time: now(),
      error: true
    })
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

function onKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    send()
  }
}

function onEscape(event) {
  if (event.key === 'Escape') open.value = false
}

function clearChat() {
  messages.value = []
  localStorage.removeItem(STORAGE_KEY)
}

onMounted(() => window.addEventListener('keydown', onEscape))
onBeforeUnmount(() => window.removeEventListener('keydown', onEscape))

// 让页面右上角/其他组件也能打开助手
function handleExternalOpen() {
  open.value = true
}
onMounted(() => window.addEventListener('grade-ai-open', handleExternalOpen))
onBeforeUnmount(() => window.removeEventListener('grade-ai-open', handleExternalOpen))
</script>

<template>
  <!-- 悬浮按钮 -->
  <button
    type="button"
    class="ai-fab"
    :class="{ 'is-open': open }"
    title="AI 咨询"
    aria-label="打开 AI 咨询"
    @click="open = !open"
  >
    <svg v-if="!open" viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
    </svg>
    <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M18 6 6 18M6 6l12 12" />
    </svg>
    <span v-if="showDot" class="ai-dot"></span>
  </button>

  <!-- 对话抽屉 -->
  <transition name="ai-slide">
    <aside v-if="open" class="ai-panel" aria-label="AI 咨询面板">
      <header class="ai-head">
        <div class="ai-title">
          <span class="ai-logo">AI</span>
          <div>
            <h3>AI 学习助手</h3>
            <p>DeepSeek 驱动 · 成绩与查分咨询</p>
          </div>
        </div>
        <div class="ai-actions">
          <button type="button" class="ai-icon-btn" title="清空对话" @click="clearChat">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6M10 11v6M14 11v6" />
            </svg>
          </button>
          <button type="button" class="ai-icon-btn" title="关闭" @click="open = false">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 6 6 18M6 6l12 12" />
            </svg>
          </button>
        </div>
      </header>

      <div ref="listRef" class="ai-body">
        <div v-if="!enabled" class="ai-notice">
          管理员尚未配置 DeepSeek API Key，暂时无法对话。
        </div>

        <div v-if="!messages.length" class="ai-welcome">
          <div class="ai-avatar">AI</div>
          <p>你好{{ user.name ? `，${user.name}` : '' }}！我可以帮你分析成绩、解释查分流程，或给出学习建议。</p>
          <div class="ai-chips">
            <button
              v-for="item in QUICK_PROMPTS"
              :key="item"
              type="button"
              class="ai-chip"
              :disabled="!enabled || sending"
              @click="send(item)"
            >
              {{ item }}
            </button>
          </div>
        </div>

        <div v-for="(msg, index) in messages" :key="index" class="ai-row" :class="msg.role">
          <div v-if="msg.role === 'assistant'" class="ai-avatar">AI</div>
          <div class="ai-bubble" :class="{ 'is-error': msg.error }">
            <div
              v-if="msg.role === 'assistant' && !msg.error"
              class="ai-content"
              v-html="renderMarkdown(msg.content)"
            ></div>
            <div v-else class="ai-content">{{ msg.content }}</div>
            <span class="ai-time">{{ msg.time }}</span>
          </div>
        </div>

        <div v-if="sending" class="ai-row assistant">
          <div class="ai-avatar">AI</div>
          <div class="ai-bubble ai-typing"><i></i><i></i><i></i></div>
        </div>
      </div>

      <footer class="ai-foot">
        <textarea
          ref="inputRef"
          v-model="input"
          class="ai-input"
          rows="2"
          placeholder="输入问题，Enter 发送，Shift + Enter 换行"
          :disabled="!enabled || sending"
          @keydown="onKeydown"
        ></textarea>
        <button
          type="button"
          class="ai-send"
          :disabled="!enabled || sending || !input.trim()"
          @click="send()"
        >
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="m22 2-7 20-4-9-9-4Z" /><path d="M22 2 11 13" />
          </svg>
        </button>
      </footer>
    </aside>
  </transition>
</template>

<style scoped>
.ai-fab {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 1400;
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #2b6cb0 100%);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(43, 108, 176, 0.38);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.ai-fab:hover {
  transform: translateY(-2px) scale(1.04);
  box-shadow: 0 12px 28px rgba(43, 108, 176, 0.45);
}

.ai-fab.is-open {
  background: linear-gradient(135deg, #4b5563 0%, #374151 100%);
  box-shadow: 0 8px 20px rgba(31, 41, 55, 0.35);
}

.ai-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #f56565;
  border: 2px solid #fff;
}

.ai-panel {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  z-index: 1500;
  width: 400px;
  max-width: 100vw;
  background: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: -12px 0 32px rgba(15, 23, 42, 0.16);
}

.ai-slide-enter-active,
.ai-slide-leave-active {
  transition: transform 0.26s cubic-bezier(0.22, 1, 0.36, 1);
}

.ai-slide-enter-from,
.ai-slide-leave-to {
  transform: translateX(105%);
}

.ai-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #2b6cb0 0%, #3b82f6 100%);
  color: #fff;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.ai-logo {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex: 0 0 34px;
}

.ai-title h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
}

.ai-title p {
  margin: 2px 0 0;
  font-size: 11px;
  opacity: 0.85;
}

.ai-actions {
  display: flex;
  gap: 6px;
}

.ai-icon-btn {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-icon-btn:hover {
  background: rgba(255, 255, 255, 0.28);
}

.ai-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f7fafc;
}

.ai-notice {
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fdf4e9;
  border: 1px solid #f0dfcd;
  color: #8f6224;
  font-size: 12px;
}

.ai-welcome {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 14px;
  font-size: 13px;
  color: #4a5568;
}

.ai-welcome p {
  margin: 8px 0 12px;
  line-height: 1.7;
}

.ai-chips {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-chip {
  text-align: left;
  padding: 8px 12px;
  border: 1px solid #d5e0ee;
  border-radius: 8px;
  background: #f5f9ff;
  color: var(--color-primary);
  font-size: 12px;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}

.ai-chip:hover {
  background: #e8f2ff;
  border-color: #b8d3f0;
}

.ai-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  align-items: flex-start;
}

.ai-row.user {
  justify-content: flex-end;
}

.ai-avatar {
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  border-radius: 8px;
  background: var(--color-primary);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-bubble {
  position: relative;
  max-width: 78%;
  padding: 10px 12px 18px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid var(--color-border);
  font-size: 13px;
  line-height: 1.7;
  color: #2d3748;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-content :deep(strong) {
  color: #1a202c;
  font-weight: 700;
}

.ai-content :deep(.md-li) {
  display: block;
  padding-left: 4px;
}

.ai-row.user .ai-bubble {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-row.assistant .ai-bubble {
  border-bottom-left-radius: 4px;
}

.ai-bubble.is-error {
  background: #fff5f5;
  border-color: #fed7d7;
  color: #c53030;
}

.ai-time {
  position: absolute;
  right: 10px;
  bottom: 4px;
  font-size: 10px;
  opacity: 0.55;
}

.ai-typing {
  display: flex;
  gap: 4px;
  align-items: center;
  padding-bottom: 10px;
}

.ai-typing i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #a0aec0;
  animation: ai-blink 1.2s infinite ease-in-out;
}

.ai-typing i:nth-child(2) {
  animation-delay: 0.2s;
}

.ai-typing i:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes ai-blink {
  0%, 80%, 100% { opacity: 0.25; transform: translateY(0); }
  40% { opacity: 1; transform: translateY(-2px); }
}

.ai-foot {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid var(--color-border);
  background: #fff;
  align-items: flex-end;
}

.ai-input {
  flex: 1;
  resize: none;
  border: 1px solid #cfd6de;
  border-radius: 10px;
  padding: 9px 11px;
  font-family: inherit;
  font-size: 13px;
  line-height: 1.5;
  color: var(--color-text);
  outline: none;
  max-height: 120px;
}

.ai-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(43, 108, 176, 0.12);
}

.ai-send {
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  border: none;
  border-radius: 10px;
  background: var(--color-primary);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-send:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .ai-panel {
    width: 100vw;
  }

  .ai-fab {
    right: 16px;
    bottom: 16px;
  }
}
</style>
