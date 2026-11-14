/**
 * 真实后端接口封装。
 *
 * Vite 代理：/api -> http://localhost:8080（去掉 /api 前缀），
 * 因此 /api/portal/auth/login 最终访问 /portal/auth/login。
 */
import { clearToken, getToken } from '@/utils/auth'

const BASE_URL = '/api/portal'

async function request(path, { method = 'GET', body, params } = {}) {
  const url = new URL(BASE_URL + path, window.location.origin)
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, value)
      }
    })
  }

  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const response = await fetch(url, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  })

  let payload
  try {
    payload = await response.json()
  } catch (error) {
    throw new Error(`请求失败：${response.status}`)
  }

  if (response.status === 401 || payload.code === 401) {
    clearToken()
    window.dispatchEvent(new Event('grade-auth-expired'))
    throw new Error('登录已过期，请重新登录')
  }
  if (payload.code !== 200) {
    throw new Error(payload.msg || '请求失败')
  }
  return payload.data
}

export const api = {
  login: (payload) => request('/auth/login', { method: 'POST', body: payload }),
  profile: () => request('/auth/profile'),
  options: () => request('/options'),

  queryScores: (params) => request('/scores', { params }),
  roster: (params) => request('/scores/roster', { params }),
  logs: (scoreId) => request(`/scores/${scoreId}/logs`),
  saveScores: (payload) => request('/scores/batch', { method: 'POST', body: payload }),
  updateScore: (scoreId, payload) => request(`/scores/${scoreId}`, { method: 'PUT', body: payload }),

  submitAppeal: (payload) => request('/appeals', { method: 'POST', body: payload }),
  myAppeals: () => request('/appeals/mine'),
  appealList: (params) => request('/appeals', { params }),
  handleAppeal: (appealId, payload) => request(`/appeals/${appealId}/handle`, { method: 'PUT', body: payload }),
  rejectAppeal: (appealId, payload) => request(`/appeals/${appealId}/reject`, { method: 'PUT', body: payload })
}

export default api
