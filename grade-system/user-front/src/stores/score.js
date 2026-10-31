import { defineStore } from 'pinia'
import { api } from '@/api/request'
import { useUserStore } from '@/stores/user'

/**
 * 成绩 / 查分申请 / 修改记录 —— 服务端数据的本地缓存。
 *
 * 视图层继续使用原来的同步 getter（scoreRowsOfStudent / appealRowsByStatus 等），
 * 数据加载与写操作改为 async action；这样 6 个页面的 UI 与业务规则保持不变。
 */
export const useScoreStore = defineStore('score', {
  state: () => ({
    terms: [],
    classes: [],
    courses: [],
    scores: [],
    appeals: [],
    roster: [],
    logsByScore: {},
    loaded: false,
    loading: false
  }),

  getters: {
    termOptions: (state) => state.terms,
    classOptions: (state) => state.classes,
    courseList: (state) => state.courses,
    studentList: (state) => {
      const map = {}
      state.scores.forEach((row) => {
        map[row.studentId] = {
          studentId: row.studentId,
          name: row.studentName,
          classId: row.classId,
          className: row.className
        }
      })
      return Object.values(map)
    },

    courseMap: (state) => Object.fromEntries(state.courses.map((item) => [item.courseId, item])),
    studentMap: (state) => {
      const map = {}
      state.scores.forEach((row) => {
        map[row.studentId] = {
          studentId: row.studentId,
          name: row.studentName,
          classId: row.classId,
          className: row.className
        }
      })
      return map
    },

    userNameOf() {
      return (loginId) => {
        const student = this.studentMap[loginId]
        if (student) return student.name
        for (const list of Object.values(this.logsByScore)) {
          const hit = list.find((log) => log.operatorId === loginId)
          if (hit && hit.operatorName) return hit.operatorName
        }
        return loginId
      }
    },

    /** 后端已经返回 status；保留推导逻辑作为兜底 */
    scoreStatusOf: (state) => (score) => {
      if (!score) return 'NORMAL'
      if (score.status) return score.status
      const pending = state.appeals.some(
        (item) =>
          item.studentId === score.studentId &&
          item.courseId === score.courseId &&
          item.term === score.term &&
          item.status === 'PENDING'
      )
      return pending ? 'APPEALING' : 'NORMAL'
    }
  },

  actions: {
    async ensureLoaded() {
      if (this.loaded || this.loading) return
      await this.bootstrap()
    },

    async bootstrap() {
      this.loading = true
      try {
        await Promise.all([this.fetchOptions(), this.fetchScores(), this.fetchAppeals()])
        this.loaded = true
      } finally {
        this.loading = false
      }
    },

    async fetchOptions() {
      const data = await api.options()
      this.terms = data?.terms || []
      this.classes = data?.classes || []
      this.courses = data?.courses || []
    },

    async fetchScores(params = {}) {
      const rows = await api.queryScores(params)
      this.scores = rows || []
      return this.scores
    },

    async fetchAppeals() {
      const user = useUserStore()
      const rows = user.isStudent ? await api.myAppeals() : await api.appealList({})
      this.appeals = rows || []
      return this.appeals
    },

    async fetchMine() {
      this.appeals = (await api.myAppeals()) || []
      return this.appeals
    },

    async fetchRoster(classId, courseId, term) {
      this.roster = (await api.roster({ classId, courseId, term })) || []
      return this.roster
    },

    async fetchLogs(scoreId) {
      const rows = (await api.logs(scoreId)) || []
      this.logsByScore = { ...this.logsByScore, [scoreId]: rows }
      return rows
    },

    /** 学生：查询本人成绩（支持学期 + 课程关键字） */
    scoreRowsOfStudent(studentId, { term = '', keyword = '' } = {}) {
      const text = String(keyword || '').trim()
      return this.scores
        .filter((score) => score.studentId === studentId)
        .filter((score) => !term || score.term === term)
        .map((score) => ({
          ...score,
          status: this.scoreStatusOf(score)
        }))
        .filter((row) => {
          if (!text) return true
          return row.courseName.includes(text) || row.courseId.includes(text)
        })
        .sort((a, b) => String(a.courseId).localeCompare(String(b.courseId)))
    },

    appealRowsOfStudent(studentId) {
      return [...this.appeals]
        .filter((item) => item.studentId === studentId)
        .sort((a, b) => String(b.applyTime || '').localeCompare(String(a.applyTime || '')))
    },

    hasPendingAppeal(studentId, courseId, term) {
      return this.appeals.some(
        (item) =>
          item.studentId === studentId &&
          item.courseId === courseId &&
          (!term || item.term === term) &&
          item.status === 'PENDING'
      )
    },

    appealRowsByStatus(status) {
      const list =
        status === 'PENDING'
          ? this.appeals.filter((item) => item.status === 'PENDING')
          : this.appeals.filter((item) => item.status !== 'PENDING')
      return [...list].sort((a, b) =>
        String(b.applyTime || '').localeCompare(String(a.applyTime || ''))
      )
    },

    rosterOf() {
      return this.roster
    },

    scoreOf(studentId, courseId, term) {
      return this.scores.find(
        (item) =>
          item.studentId === studentId &&
          item.courseId === courseId &&
          (!term || item.term === term)
      )
    },

    logsOf(studentId, courseId) {
      const score = this.scores.find(
        (item) => item.studentId === studentId && item.courseId === courseId
      )
      return score ? this.logsByScore[score.scoreId] || [] : []
    },

    async submitAppeal({ courseId, reason }) {
      const appeal = await api.submitAppeal({ courseId, reason })
      await Promise.all([this.fetchMine(), this.fetchScores()])
      return appeal
    },

    async saveBatch({ classId, courseId, term, rows }) {
      const result = await api.saveScores({ classId, courseId, term, rows })
      await this.fetchScores()
      await this.fetchRoster(classId, courseId, term)
      return result?.savedCount ?? 0
    },

    async updateScore(payload) {
      let scoreId = payload.scoreId
      if (!scoreId) {
        const found = this.scores.find(
          (item) =>
            item.studentId === payload.studentId &&
            item.courseId === payload.courseId &&
            item.term === payload.term
        )
        scoreId = found?.scoreId
      }
      if (!scoreId) return null

      const updated = await api.updateScore(scoreId, {
        usualScore: payload.usualScore,
        finalScore: payload.finalScore,
        reason: payload.reason
      })
      await this.fetchScores()
      await this.fetchLogs(scoreId)
      return updated
    },

    async handleAppeal(appealId, { result, correctedScore, opinion }) {
      const updated = await api.handleAppeal(appealId, { result, correctedScore, opinion })
      await Promise.all([this.fetchAppeals(), this.fetchScores()])
      return updated
    },

    async rejectAppeal(appealId, { opinion }) {
      const updated = await api.rejectAppeal(appealId, { opinion })
      await this.fetchAppeals()
      return updated
    },

    reset() {
      this.scores = []
      this.appeals = []
      this.roster = []
      this.logsByScore = {}
      this.loaded = false
    }
  }
})
