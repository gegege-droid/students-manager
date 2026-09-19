/** 格式化工具 */

function pad(n) {
  return String(n).padStart(2, '0')
}

/** Date → 'YYYY-MM-DD HH:mm' */
export function formatDateTime(date = new Date()) {
  const d = date instanceof Date ? date : new Date(date)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** Date → 'YYYY-MM-DD' */
export function formatDate(date = new Date()) {
  const d = date instanceof Date ? date : new Date(date)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** Date → 'YYYYMMDD'，用于生成申请编号 */
export function formatCompactDate(date = new Date()) {
  const d = date instanceof Date ? date : new Date(date)
  return `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}`
}

/** 截断文本，超出加省略号 */
export function ellipsis(text, max = 24) {
  if (!text) return ''
  return text.length > max ? `${text.slice(0, max)}…` : text
}
