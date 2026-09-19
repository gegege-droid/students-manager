import { defineStore } from 'pinia'
import { accounts } from '@/api/mock'

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

/** 用户会话（学生 / 班主任） */
export const useUserStore = defineStore('user', {
  state: () => ({
    loginId: cached?.loginId || '',
    role: cached?.role || '',
    name: cached?.name || '',
    classId: cached?.classId || '',
    className: cached?.className || '',
    manageClassIds: cached?.manageClassIds || []
  }),

  getters: {
    isLoggedIn: (state) => Boolean(state.loginId),
    isStudent: (state) => state.role === 'STUDENT',
    isTeacher: (state) => state.role === 'TEACHER',
    roleText: (state) =>
      state.role === 'STUDENT' ? '学生' : state.role === 'TEACHER' ? '班主任' : '',
    /** 登录后落在本角色首页 */
    homeRoute: (state) =>
      state.role === 'TEACHER' ? 'TeacherAppealHandle' : 'StudentExamQuery'
  },

  actions: {
    login(loginId, password) {
      const account = accounts.find((item) => item.loginId === String(loginId || '').trim())
      if (!account || account.password !== password) {
        return { ok: false, message: '学号/工号或密码错误，请重新输入' }
      }
      this.loginId = account.loginId
      this.role = account.role
      this.name = account.name
      this.classId = account.classId || ''
      this.className = account.className || ''
      this.manageClassIds = account.manageClassIds || []
      this.persist()
      return { ok: true, role: account.role }
    },

    logout() {
      this.loginId = ''
      this.role = ''
      this.name = ''
      this.classId = ''
      this.className = ''
      this.manageClassIds = []
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
