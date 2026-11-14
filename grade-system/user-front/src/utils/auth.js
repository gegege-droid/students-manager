/** 登录令牌存储（localStorage，key 与用户信息分开保存） */
const TOKEN_KEY = 'grade-system:token'

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY) || ''
  } catch (error) {
    return ''
  }
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token || '')
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}
