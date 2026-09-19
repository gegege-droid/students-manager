/**
 * 接口封装占位。
 *
 * 当前版本所有数据都来自 src/api/mock.js + localStorage，
 * 不需要真实后端即可完整演示 6 个页面的交互闭环。
 *
 * 需要接真实后端时，把下面这些函数改成 fetch 调用即可，
 * 接口清单见 docs/04-data-and-vue.md §6。
 */

const BASE_URL = '/api'

async function request(path, { method = 'GET', body, params } = {}) {
  const url = new URL(BASE_URL + path, window.location.origin)
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') url.searchParams.set(key, value)
    })
  }
  const response = await fetch(url, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined
  })
  if (!response.ok) throw new Error(`请求失败：${response.status}`)
  return response.json()
}

export const api = {
  login: (payload) => request('/auth/login', { method: 'POST', body: payload }),
  queryScores: (params) => request('/scores', { params }),
  submitAppeal: (payload) => request('/appeals', { method: 'POST', body: payload }),
  myAppeals: () => request('/appeals/mine'),
  saveScores: (payload) => request('/scores/batch', { method: 'POST', body: payload }),
  updateScore: (scoreId, payload) => request(`/scores/${scoreId}`, { method: 'PUT', body: payload }),
  appealList: (params) => request('/appeals', { params }),
  handleAppeal: (appealId, payload) => request(`/appeals/${appealId}/handle`, { method: 'PUT', body: payload })
}

export default api
