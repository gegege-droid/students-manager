import { defineStore } from 'pinia'
import {
  accounts,
  appeals as seedAppeals,
  changeLogs as seedLogs,
  classes,
  courses,
  scores as seedScores,
  students,
  terms
} from '@/api/mock'
import { calcGpa, calcTotal } from '@/utils/score'
import { formatCompactDate, formatDateTime } from '@/utils/format'

const STORAGE_KEY = 'grade-system:data'
/** 演示数据版本号：修改 mock 数据后 +1，可让浏览器里的旧缓存自动失效 */
const DATA_VERSION = 1

/** 补全计算字段（总评 / 绩点）。已手工更正过的总评不再被公式覆盖 */
function normalizeScore(raw) {
  const total = raw.manualTotal ? raw.totalScore : calcTotal(raw.usualScore, raw.finalScore)
  return { ...raw, totalScore: total, gpa: calcGpa(total) }
}

function createSeedData() {
  return {
    version: DATA_VERSION,
    scores: seedScores.map(normalizeScore),
    appeals: seedAppeals.map((item) => ({ ...item })),
    logs: seedLogs.map((item) => ({ ...item }))
  }
}

function loadData() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (parsed && parsed.version === DATA_VERSION) {
        parsed.scores = parsed.scores.map(normalizeScore)
        return parsed
      }
    }
  } catch (error) {
    console.warn('读取演示数据失败，将重置为初始数据', error)
  }
  return createSeedData()
}

function sortByTimeDesc(list, key) {
  return [...list].sort((a, b) => String(b[key] || '').localeCompare(String(a[key] || '')))
}

/**
 * 成绩 / 查分申请 / 修改记录 —— 全站唯一数据源。
 * 用 localStorage 持久化，这样演示时「学生提交申请 → 班主任就能看到」，
 * 刷新页面数据也不会丢。
 */
export const useScoreStore = defineStore('score', {
  state: () => loadData(),

  getters: {
    /** 学期下拉选项 */
    termOptions: () => terms,
    /** 班级下拉选项 */
    classOptions: () => classes,
    /** 课程列表 */
    courseList: () => courses,
    /** 学生名单 */
    studentList: () => students,

    courseMap: () => Object.fromEntries(courses.map((item) => [item.courseId, item])),
    studentMap: () => Object.fromEntries(students.map((item) => [item.studentId, item])),

    /** 工号/学号 → 姓名。视图层不直接读 api/mock，统一从这里取 */
    userNameOf: () => (loginId) => accounts.find((item) => item.loginId === loginId)?.name || loginId,

    /** 成绩状态：查分中 > 已更正 > 正常 —— 对应需求 §5.2 状态机 */
    scoreStatusOf: (state) => (score) => {
      if (!score) return 'NORMAL'
      const pending = state.appeals.some(
        (item) =>
          item.studentId === score.studentId &&
          item.courseId === score.courseId &&
          item.term === score.term &&
          item.status === 'PENDING'
      )
      if (pending) return 'APPEALING'
      const modified = state.logs.some(
        (item) => item.studentId === score.studentId && item.courseId === score.courseId
      )
      return modified ? 'MODIFIED' : 'NORMAL'
    }
  },

  actions: {
    persist() {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.$state))
      } catch (error) {
        console.warn('保存演示数据失败', error)
      }
    },

    /** 恢复初始演示数据（登录页提供入口，方便演示前重置） */
    resetDemo() {
      const seed = createSeedData()
      this.scores = seed.scores
      this.appeals = seed.appeals
      this.logs = seed.logs
      this.persist()
    },

    // ==================== 学生侧 ====================

    /**
     * 查询某学生的成绩（带课程信息），支持学期 + 关键字筛选
     * 对应 FR-P2-02 / 03
     */
    scoreRowsOfStudent(studentId, { term = '', keyword = '' } = {}) {
      const text = String(keyword || '').trim()
      return this.scores
        .filter((score) => score.studentId === studentId)
        .filter((score) => !term || score.term === term)
        .map((score) => {
          const course = this.courseMap[score.courseId] || {}
          return {
            ...score,
            courseName: course.courseName || score.courseId,
            credit: course.credit ?? 0,
            courseType: course.courseType || 'REQUIRED',
            status: this.scoreStatusOf(score)
          }
        })
        .filter((row) => {
          if (!text) return true
          return row.courseName.includes(text) || row.courseId.includes(text)
        })
        .sort((a, b) => String(a.courseId).localeCompare(String(b.courseId)))
    },

    /** 某学生的查分申请记录（按时间倒序） */
    appealRowsOfStudent(studentId) {
      return sortByTimeDesc(
        this.appeals.filter((item) => item.studentId === studentId),
        'applyTime'
      ).map((item) => ({ ...item, courseName: this.courseMap[item.courseId]?.courseName || item.courseId }))
    },

    /** 该生该课程是否已有待受理申请 —— 对应 FR-P3-08 防重复提交 */
    hasPendingAppeal(studentId, courseId, term) {
      return this.appeals.some(
        (item) =>
          item.studentId === studentId &&
          item.courseId === courseId &&
          (!term || item.term === term) &&
          item.status === 'PENDING'
      )
    },

    /** 提交查分申请 —— 对应 FR-P3-05 / 06 */
    submitAppeal({ studentId, courseId, reason }) {
      const course = this.courseMap[courseId]
      const score = this.scores.find((item) => item.studentId === studentId && item.courseId === courseId)
      const term = course?.term || score?.term || terms[0]
      const appeal = {
        appealId: this.nextAppealId(),
        studentId,
        courseId,
        term,
        originalScore: score?.totalScore ?? null,
        reason: String(reason).trim(),
        applyTime: formatDateTime(),
        status: 'PENDING',
        result: '',
        correctedScore: null,
        opinion: '',
        handlerId: '',
        handleTime: ''
      }
      this.appeals.unshift(appeal)
      this.persist()
      return appeal
    },

    nextAppealId() {
      const base = `AP${formatCompactDate()}`
      let index = 1
      let candidate = `${base}${String(index).padStart(3, '0')}`
      while (this.appeals.some((item) => item.appealId === candidate)) {
        index += 1
        candidate = `${base}${String(index).padStart(3, '0')}`
      }
      return candidate
    },

    // ==================== 班主任侧 ====================

    /** 待受理 / 已办结列表（带学生与课程信息） */
    appealRowsByStatus(status) {
      const list = status === 'PENDING'
        ? this.appeals.filter((item) => item.status === 'PENDING')
        : this.appeals.filter((item) => item.status !== 'PENDING')
      return sortByTimeDesc(list, 'applyTime').map((item) => this.decorateAppeal(item))
    },

    decorateAppeal(item) {
      const student = this.studentMap[item.studentId] || {}
      const course = this.courseMap[item.courseId] || {}
      return {
        ...item,
        studentName: student.name || item.studentId,
        className: student.className || '',
        courseName: course.courseName || item.courseId,
        credit: course.credit ?? 0
      }
    },

    /** 某班某课程的成绩明细（P4 批量录入用） */
    rosterOf(classId, courseId, term) {
      return students
        .filter((student) => !classId || student.classId === classId)
        .map((student) => {
          const score = this.scores.find(
            (item) =>
              item.studentId === student.studentId && item.courseId === courseId && item.term === term
          )
          return {
            ...student,
            scoreId: score?.scoreId || '',
            usualScore: score?.usualScore ?? null,
            finalScore: score?.finalScore ?? null,
            totalScore: score?.totalScore ?? null,
            recorded: Boolean(score)
          }
        })
    },

    scoreOf(studentId, courseId, term) {
      return this.scores.find(
        (item) => item.studentId === studentId && item.courseId === courseId && (!term || item.term === term)
      )
    },

    /** 成绩修改记录 */
    logsOf(studentId, courseId) {
      return sortByTimeDesc(
        this.logs.filter(
          (item) => item.studentId === studentId && (!courseId || item.courseId === courseId)
        ),
        'operateTime'
      ).map((item) => ({
        ...item,
        studentName: this.studentMap[item.studentId]?.name || item.studentId,
        courseName: this.courseMap[item.courseId]?.courseName || item.courseId
      }))
    },

    /** 批量录入成绩 —— 对应 FR-P4-07 */
    saveBatch({ courseId, term, rows, recorderId }) {
      let saved = 0
      const now = formatDateTime()
      rows.forEach((row) => {
        const hasUsual = row.usualScore !== null && row.usualScore !== undefined && row.usualScore !== ''
        const hasFinal = row.finalScore !== null && row.finalScore !== undefined && row.finalScore !== ''
        if (!hasUsual && !hasFinal) return

        const existing = this.scores.find(
          (item) => item.studentId === row.studentId && item.courseId === courseId && item.term === term
        )
        const usual = hasUsual ? Number(row.usualScore) : null
        const final = hasFinal ? Number(row.finalScore) : null

        if (existing) {
          if (hasUsual) existing.usualScore = usual
          if (hasFinal) existing.finalScore = final
          const total = calcTotal(existing.usualScore, existing.finalScore)
          existing.totalScore = total
          existing.gpa = calcGpa(total)
          delete existing.manualTotal
        } else {
          const total = calcTotal(usual, final)
          this.scores.push({
            scoreId: `SC${Date.now()}${saved}`,
            studentId: row.studentId,
            courseId,
            term,
            usualScore: usual,
            finalScore: final,
            totalScore: total,
            gpa: calcGpa(total),
            recorderId,
            recordTime: now
          })
        }
        saved += 1
      })
      this.persist()
      return saved
    },

    /** 修改单条成绩并留痕 —— 对应 FR-P5-05 / 06 */
    updateScore({ studentId, courseId, term, usualScore, finalScore, reason, operatorId }) {
      const score = this.scoreOf(studentId, courseId, term)
      if (!score) return null

      const oldUsual = score.usualScore
      const oldFinal = score.finalScore
      const oldTotal = score.totalScore

      const total = calcTotal(usualScore, finalScore)
      score.usualScore = Number(usualScore)
      score.finalScore = Number(finalScore)
      score.totalScore = total
      score.gpa = calcGpa(total)
      delete score.manualTotal

      const log = {
        logId: `LOG${Date.now()}`,
        scoreId: score.scoreId,
        studentId,
        courseId,
        term,
        oldUsual,
        oldFinal,
        oldTotal,
        newUsual: score.usualScore,
        newFinal: score.finalScore,
        newTotal: total,
        changeReason: String(reason).trim(),
        operatorId,
        operateTime: formatDateTime(),
        source: 'MANUAL'
      }
      this.logs.unshift(log)
      this.persist()
      return { score: { ...score }, log }
    },

    /**
     * 受理查分申请 —— 对应 FR-P6-04 ~ 07
     * 「更正成绩」会同步更新该生该课程的总评并写入修改记录（留痕）
     */
    handleAppeal(appealId, { result, correctedScore, opinion, handlerId }) {
      const appeal = this.appeals.find((item) => item.appealId === appealId)
      if (!appeal) return null

      appeal.status = 'ACCEPTED'
      appeal.result = result
      appeal.opinion = String(opinion).trim()
      appeal.handlerId = handlerId
      appeal.handleTime = formatDateTime()

      if (result === 'CORRECT' && correctedScore !== null && correctedScore !== undefined) {
        appeal.correctedScore = Number(correctedScore)
        const score = this.scoreOf(appeal.studentId, appeal.courseId, appeal.term)
        if (score) {
          const oldTotal = score.totalScore
          score.totalScore = Number(correctedScore)
          score.gpa = calcGpa(score.totalScore)
          score.manualTotal = true
          this.logs.unshift({
            logId: `LOG${Date.now()}`,
            scoreId: score.scoreId,
            studentId: score.studentId,
            courseId: score.courseId,
            term: score.term,
            oldUsual: score.usualScore,
            oldFinal: score.finalScore,
            oldTotal,
            newUsual: score.usualScore,
            newFinal: score.finalScore,
            newTotal: score.totalScore,
            changeReason: `查分受理更正：${appeal.opinion}`,
            operatorId: handlerId,
            operateTime: appeal.handleTime,
            source: 'APPEAL'
          })
        }
      }

      this.persist()
      return this.decorateAppeal(appeal)
    },

    /** 驳回查分申请 —— 对应 FR-P6-10 */
    rejectAppeal(appealId, { opinion, handlerId }) {
      const appeal = this.appeals.find((item) => item.appealId === appealId)
      if (!appeal) return null
      appeal.status = 'REJECTED'
      appeal.result = ''
      appeal.opinion = String(opinion).trim()
      appeal.handlerId = handlerId
      appeal.handleTime = formatDateTime()
      this.persist()
      return this.decorateAppeal(appeal)
    }
  }
})
