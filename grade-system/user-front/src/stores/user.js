import { defineStore } from 'pinia'
import { api } from '@/api/request'
import { clearToken, getToken, setToken } from '@/utils/auth'

const STORAGE_KEY = 'grade-system:user'

function readCachedUser() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw)
  } catch (error) {
    console.warn('读取登录信息失败，将使用空会话', error)
  }
  return null
}

const cached = readCachedUser()

/**
 * 用户会话 —— 对应 Axure 的全局变量 LoginUser / Role。
 * 登录成功后端返回 JWT，前端保存 token 与身份信息；刷新页面可恢复会话。
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    loginId: cached?.loginId || '',
    role: cached?.role || '',
    name: cached?.name || '',
    classId: cached?.classId || '',
    className: cached?.className || '',
    manageClassIds: cached?.manageClassIds || [],
    token: getToken()
  }),

  getters: {
    isLoggedIn: (state) => Boolean(state.token && state.loginId),
    isStudent: (state) => state.role === 'STUDENT',
    isTeacher: (state) => state.role === 'TEACHER',
    roleText: (state) => (state.role === 'STUDENT' ? '学生' : state.role === 'TEACHER' ? '班主任' : ''),
    /** 登录后该角色应该落在哪个首页 —— 对应 FR-P1-06 角色分流 */
    homeRoute: (state) => (state.role === 'TEACHER' ? 'TeacherAppealHandle' : 'StudentScoreQuery')
  },

  actions: {
    applyUser(user) {
      this.loginId = user.loginId || ''
      this.role = user.role || ''
      this.name = user.name || ''
      this.classId = user.classId || ''
      this.className = user.className || ''
      this.manageClassIds = Array.isArray(user.manageClassIds) ? user.manageClassIds : []
      this.token = getToken()
      this.persist()
    },

    /**
     * 登录：调用后端 /portal/auth/login，成功后保存 token 与身份。
     * @returns {Promise<{ok: boolean, message?: string, role?: string}>}
     */
    async login(loginId, password) {
      try {
        const data = await api.login({
          username: String(loginId || '').trim(),
          password: String(password || '')
        })
        clearToken()
        setToken(data.token)
        this.applyUser(data.user || {})
        return { ok: true, role: this.role }
      } catch (error) {
        return { ok: false, message: error.message || '账号或密码错误，请重新输入' }
      }
    },

    async refreshProfile() {
      if (!getToken()) return false
      try {
        const user = await api.profile()
        this.applyUser(user)
        return true
      } catch (error) {
        this.logout()
        return false
      }
    },

    logout() {
      this.loginId = ''
      this.role = ''
      this.name = ''
      this.classId = ''
      this.className = ''
      this.manageClassIds = []
      this.token = ''
      clearToken()
      this.persist()
    },

    persist() {
      try {
        localStorage.setItem(
          STORAGE_KEY,
          JSON.stringify({
            loginId: this.loginId,
            role: this.role,
            name: this.name,
            classId: this.classId,
            className: this.className,
            manageClassIds: this.manageClassIds
          })
        )
      } catch (error) {
        console.warn('保存登录信息失败', error)
      }
    }
  }
})
