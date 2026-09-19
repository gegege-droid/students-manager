import { ref } from 'vue'

/**
 * 全局轻提示（对应 Axure 母版 M_提示_成功）。
 * 放在模块作用域，任何组件调用 useToast() 拿到的都是同一份队列。
 */
const toasts = ref([])
let seq = 0

function push(message, type = 'info', duration = 2400) {
  const id = (seq += 1)
  toasts.value.push({ id, message, type })
  window.setTimeout(() => {
    toasts.value = toasts.value.filter((item) => item.id !== id)
  }, duration)
}

export function useToast() {
  return {
    toasts,
    show: push,
    success: (message, duration) => push(message, 'success', duration),
    error: (message, duration) => push(message, 'error', duration),
    info: (message, duration) => push(message, 'info', duration)
  }
}
