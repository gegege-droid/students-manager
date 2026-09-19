import { defineStore } from 'pinia'
import {
  accounts,
  appeals as seedAppeals,
  changeLogs as seedLogs,
  EXAM_SESSIONS,
  GRADE_INFO,
  REFERENCE_LINES,
  SCORED_SESSIONS,
  STUDENTS,
  SUBJECTS,
  scores as seedScores
} from '@/api/mock'
import { buildRankMap, estimateGradeRank, predictSubject, sumPredictions } from '@/utils/score'
import { formatCompactDate, formatDateTime } from '@/utils/format'

const STORAGE_KEY = 'grade-system:data'
/** 演示数据版本号：改过 mock 数据后 +1，浏览器里的旧缓存会自动失效 */
const DATA_VERSION = 4

function createSeedData() {
  return {
    version: DATA_VERSION,
    scores: seedScores.map((item) => ({ ...item, recordTime: '2027-04-11 18:00' })),
    appeals: seedAppeals.map((item) => ({ ...item })),
    logs: seedLogs.map((item) => ({ ...item }))
  }
}

function loadData() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (parsed && parsed.version === DATA_VERSION) return parsed
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
 * 高中成绩 / 查分申请 / 修改记录 —— 全站唯一数据源
 *
 * 维度：学生 × 月考场次 × 科目
 * 用 localStorage 持久化，演示时「学生提交查分 → 班主任就能看到」跨角色闭环。
 */
export const useScoreStore = defineStore('score', {
  state: () => loadData(),

  getters: {
    subjectList: () => SUBJECTS,
    /** 全部场次（含尚未录入的），用于录入页下拉 */
    sessionList: () => EXAM_SESSIONS,
    /** 已出成绩的场次，用于成绩查询 / 预测 / 筛选 */
    scoredSessionList: () => SCORED_SESSIONS,
    studentList: () => STUDENTS,
    gradeInfo: () => GRADE_INFO,
    referenceLines: () => REFERENCE_LINES,

    subjectMap: () => Object.fromEntries(SUBJECTS.map((item) => [item.subjectId, item])),
    sessionMap: () => Object.fromEntries(EXAM_SESSIONS.map((item) => [item.examId, item])),
    studentMap: () => Object.fromEntries(STUDENTS.map((item) => [item.studentId, item])),

    /** 快速索引：`学号|场次|科目` → 成绩记录，避免每次渲染都遍历数组 */
    scoreIndex: (state) => {
      const map = {}
      state.scores.forEach((item) => {
        map[`${item.studentId}|${item.examId}|${item.subjectId}`] = item
      })
      return map
    },

    /**
     * 每次月考的全班总分与排名（含估算的年级排名）
     * 返回：{ [examId]: [{ studentId, total, rate, classRank, gradeRank }] }
     */
    examRankings(state) {
      const result = {}
      SCORED_SESSIONS.forEach((session) => {
        const rows = STUDENTS.map((student) => {
          let total = 0
          let count = 0
          student.subjects.forEach((subjectId) => {
            const record = state.scores.find(
              (item) =>
                item.studentId === student.studentId &&
                item.examId === session.examId &&
                item.subjectId === subjectId
            )
            if (record) {
              total += record.score
              count += 1
            }
          })
          return { studentId: student.studentId, total: count ? total : null, rate: count ? total / GRADE_INFO.totalFullScore : null }
        })

        const rankMap = buildRankMap(rows, 'total')
        result[session.examId] = rows.map((row) => ({
          ...row,
          classRank: rankMap[row.studentId] ?? null,
          gradeRank: estimateGradeRank(row.total, GRADE_INFO.totalFullScore, GRADE_INFO.gradeStudentCount)
        }))
      })
      return result
    },

    /** 工号 / 学号 → 姓名 */
    userNameOf: () => (loginId) => accounts.find((item) => item.loginId === loginId)?.name || loginId
  },

  actions: {
    persist() {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.$state))
      } catch (error) {
        console.warn('保存演示数据失败', error)
      }
    },

    resetDemo() {
      const seed = createSeedData()
      this.scores = seed.scores
      this.appeals = seed.appeals
      this.logs = seed.logs
      this.persist()
    },

    scoreOf(studentId, examId, subjectId) {
      return this.scoreIndex[`${studentId}|${examId}|${subjectId}`] || null
    },

    rankingOf(studentId, examId) {
      const list = this.examRankings[examId] || []
      return list.find((item) => item.studentId === studentId) || null
    },

    /** 成绩状态：查分中 > 已更正 > 正常 */
    statusOf(studentId, examId, subjectId) {
      const pending = this.appeals.some(
        (item) =>
          item.studentId === studentId &&
          item.examId === examId &&
          item.subjectId === subjectId &&
          item.status === 'PENDING'
      )
      if (pending) return 'APPEALING'
      const modified = this.logs.some(
        (item) => item.studentId === studentId && item.examId === examId && item.subjectId === subjectId
      )
      return modified ? 'MODIFIED' : 'NORMAL'
    },

    // ==================== 学生侧：成绩单 ====================

    /**
     * 生成某学生的「科目 × 月考」成绩单矩阵
     * 行 = 该生选考的 6 门科目；列 = 6 次月考；另附每次月考的总分与排名
     */
    matrixOfStudent(studentId, { examId = '', subjectId = '' } = {}) {
      const student = this.studentMap[studentId]
      if (!student) return { rows: [], totals: [], sessions: [], fullScore: 0 }

      const sessions = examId
        ? SCORED_SESSIONS.filter((item) => item.examId === examId)
        : SCORED_SESSIONS

      const subjectIds = subjectId ? [subjectId] : student.subjects

      const rows = subjectIds.map((sid) => {
        const subject = this.subjectMap[sid]
        const cells = SCORED_SESSIONS.map((session) => {
          const record = this.scoreOf(studentId, session.examId, sid)
          return {
            examId: session.examId,
            score: record ? record.score : null,
            rate: record ? record.score / subject.fullScore : null,
            status: record ? this.statusOf(studentId, session.examId, sid) : 'NORMAL'
          }
        })
        const valid = cells.filter((cell) => cell.score !== null)
        const first = valid[0]
        const last = valid[valid.length - 1]
        const sum = valid.reduce((total, cell) => total + cell.rate, 0)
        return {
          subjectId: sid,
          name: subject.name,
          fullScore: subject.fullScore,
          category: subject.category,
          cells,
          latest: last ? last.score : null,
          averageRate: valid.length ? sum / valid.length : null,
          /** 与上一次相比的分数变化，用于趋势列 */
          delta: valid.length >= 2 ? last.score - valid[valid.length - 2].score : 0,
          firstScore: first ? first.score : null,
          improved: valid.length >= 2 ? last.score - valid[0].score : 0
        }
      })

      const totals = sessions.map((session) => {
        const ranking = this.rankingOf(studentId, session.examId)
        return {
          examId: session.examId,
          name: session.name,
          total: ranking ? ranking.total : null,
          rate: ranking ? ranking.rate : null,
          classRank: ranking ? ranking.classRank : null,
          gradeRank: ranking ? ranking.gradeRank : null
        }
      })

      return {
        rows,
        totals,
        sessions,
        fullScore: subjectId ? this.subjectMap[subjectId]?.fullScore || 0 : GRADE_INFO.totalFullScore,
        student
      }
    },

    /** 某学生某次月考的详情（各科分数 + 排名） */
    examDetailOfStudent(studentId, examId) {
      const student = this.studentMap[studentId]
      if (!student) return null
      const session = this.sessionMap[examId]
      const ranking = this.rankingOf(studentId, examId)
      const rows = student.subjects.map((sid) => {
        const subject = this.subjectMap[sid]
        const record = this.scoreOf(studentId, examId, sid)
        return {
          subjectId: sid,
          name: subject.name,
          fullScore: subject.fullScore,
          score: record ? record.score : null,
          rate: record ? record.score / subject.fullScore : null,
          status: record ? this.statusOf(studentId, examId, sid) : 'NORMAL'
        }
      })
      return { session, ranking, rows }
    },

    // ==================== 学生侧：高考成绩预测 ====================

    /**
     * 高考成绩预测（加权移动平均 + 趋势修正）
     * 逐科预测，再汇总成总分，并给出预测区间与置信度。
     */
    predictionOfStudent(studentId) {
      const student = this.studentMap[studentId]
      if (!student) return { subjects: [], total: null, sessionCount: 0 }

      const subjects = student.subjects.map((sid) => {
        const subject = this.subjectMap[sid]
        const records = SCORED_SESSIONS.map((session) => {
          const record = this.scoreOf(studentId, session.examId, sid)
          return { examId: session.examId, score: record ? record.score : null }
        })
        const prediction = predictSubject(records, subject.fullScore)
        return {
          subjectId: sid,
          name: subject.name,
          category: subject.category,
          fullScore: subject.fullScore,
          records,
          ...(prediction || {
            count: 0,
            latest: null,
            predicted: null,
            low: null,
            high: null,
            baseRate: null,
            predictedRate: null,
            deltaPerExam: 0,
            confidence: 'LOW'
          })
        }
      })

      const usable = subjects.filter((item) => item.predicted !== null)
      const total = usable.length ? sumPredictions(usable) : null

      // 历史总分序列（用于趋势图）
      const totalSeries = SCORED_SESSIONS.map((session) => {
        const ranking = this.rankingOf(studentId, session.examId)
        return { examId: session.examId, name: session.name, value: ranking ? ranking.total : null }
      })

      return {
        subjects,
        total,
        totalSeries,
        sessionCount: SCORED_SESSIONS.length,
        /** 整体置信度取各科里最低的一档，偏保守 */
        confidence: usable.some((item) => item.confidence === 'LOW')
          ? 'LOW'
          : usable.some((item) => item.confidence === 'MEDIUM')
            ? 'MEDIUM'
            : 'HIGH'
      }
    },

    // ==================== 学生侧：查分申请 ====================

    appealRowsOfStudent(studentId) {
      return sortByTimeDesc(
        this.appeals.filter((item) => item.studentId === studentId),
        'applyTime'
      ).map((item) => this.decorateAppeal(item))
    },

    hasPendingAppeal(studentId, examId, subjectId) {
      return this.appeals.some(
        (item) =>
          item.studentId === studentId &&
          item.examId === examId &&
          item.subjectId === subjectId &&
          item.status === 'PENDING'
      )
    },

    /** 该生可申请查分的「场次 × 科目」组合（有成绩、且该科属于他的选科） */
    appealableItems(studentId) {
      const student = this.studentMap[studentId]
      if (!student) return []
      const list = []
      SCORED_SESSIONS.forEach((session) => {
        student.subjects.forEach((sid) => {
          const record = this.scoreOf(studentId, session.examId, sid)
          if (!record) return
          list.push({
            key: `${session.examId}|${sid}`,
            examId: session.examId,
            subjectId: sid,
            examName: session.name,
            examDate: session.date,
            subjectName: this.subjectMap[sid].name,
            fullScore: this.subjectMap[sid].fullScore,
            score: record.score,
            pending: this.hasPendingAppeal(studentId, session.examId, sid)
          })
        })
      })
      return list.reverse()
    },

    submitAppeal({ studentId, examId, subjectId, reason }) {
      const record = this.scoreOf(studentId, examId, subjectId)
      const appeal = {
        appealId: this.nextAppealId(),
        studentId,
        examId,
        subjectId,
        originalScore: record ? record.score : null,
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
      return this.decorateAppeal(appeal)
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

    decorateAppeal(item) {
      const student = this.studentMap[item.studentId] || {}
      const session = this.sessionMap[item.examId] || {}
      const subject = this.subjectMap[item.subjectId] || {}
      return {
        ...item,
        studentName: student.name || item.studentId,
        className: student.className || '',
        combination: student.combination || '',
        examName: session.name || item.examId,
        examDate: session.date || '',
        subjectName: subject.name || item.subjectId,
        fullScore: subject.fullScore || 100
      }
    },

    // ==================== 班主任侧 ====================

    appealRowsByStatus(status) {
      const list =
        status === 'PENDING'
          ? this.appeals.filter((item) => item.status === 'PENDING')
          : this.appeals.filter((item) => item.status !== 'PENDING')
      return sortByTimeDesc(list, 'applyTime').map((item) => this.decorateAppeal(item))
    },

    /** 某次月考某科目的录入名单：只包含选考该科的学生 */
    rosterOf(classId, examId, subjectId) {
      return STUDENTS.filter((student) => !classId || student.classId === classId)
        .filter((student) => student.subjects.includes(subjectId))
        .map((student) => {
          const record = this.scoreOf(student.studentId, examId, subjectId)
          return {
            studentId: student.studentId,
            name: student.name,
            combination: student.combination,
            score: record ? record.score : null,
            recorded: Boolean(record)
          }
        })
    },

    /** 批量录入某次月考某科目的成绩 */
    saveBatch({ examId, subjectId, rows, recorderId }) {
      let saved = 0
      rows.forEach((row) => {
        if (row.score === null || row.score === undefined || row.score === '') return
        const existing = this.scoreOf(row.studentId, examId, subjectId)
        if (existing) {
          existing.score = Number(row.score)
          existing.recorderId = recorderId
        } else {
          this.scores.push({
            scoreId: `SC${Date.now()}${saved}`,
            studentId: row.studentId,
            examId,
            subjectId,
            score: Number(row.score),
            recorderId,
            recordTime: formatDateTime()
          })
        }
        saved += 1
      })
      this.persist()
      return saved
    },

    /** 按条件查询成绩记录（P5 修改页用） */
    findScores({ keyword = '', examId = '', subjectId = '', classId = '' } = {}) {
      const text = String(keyword).trim()
      return this.scores
        .map((record) => {
          const student = this.studentMap[record.studentId]
          const subject = this.subjectMap[record.subjectId]
          const session = this.sessionMap[record.examId]
          if (!student || !subject || !session) return null
          if (classId && student.classId !== classId) return null
          return {
            ...record,
            studentName: student.name,
            className: student.className,
            combination: student.combination,
            subjectName: subject.name,
            fullScore: subject.fullScore,
            examName: session.name,
            examDate: session.date,
            status: this.statusOf(record.studentId, record.examId, record.subjectId)
          }
        })
        .filter(Boolean)
        .filter((row) => {
          if (examId && row.examId !== examId) return false
          if (subjectId && row.subjectId !== subjectId) return false
          if (text) return row.studentId.includes(text) || row.studentName.includes(text)
          return true
        })
        .sort(
          (a, b) =>
            String(b.examId).localeCompare(String(a.examId)) ||
            String(a.studentId).localeCompare(String(b.studentId))
        )
    },

    /** 修改单条成绩并留痕 */
    updateScore({ studentId, examId, subjectId, newScore, reason, operatorId }) {
      const record = this.scoreOf(studentId, examId, subjectId)
      if (!record) return null

      const oldScore = record.score
      record.score = Number(newScore)
      record.recorderId = operatorId

      const log = {
        logId: `LOG${Date.now()}`,
        studentId,
        examId,
        subjectId,
        oldScore,
        newScore: record.score,
        changeReason: String(reason).trim(),
        operatorId,
        operateTime: formatDateTime(),
        source: 'MANUAL'
      }
      this.logs.unshift(log)
      this.persist()
      return { record: { ...record }, log }
    },

    logsOf(studentId, examId, subjectId) {
      return sortByTimeDesc(
        this.logs.filter(
          (item) =>
            item.studentId === studentId &&
            (!examId || item.examId === examId) &&
            (!subjectId || item.subjectId === subjectId)
        ),
        'operateTime'
      ).map((item) => ({
        ...item,
        studentName: this.studentMap[item.studentId]?.name || item.studentId,
        examName: this.sessionMap[item.examId]?.name || item.examId,
        subjectName: this.subjectMap[item.subjectId]?.name || item.subjectId
      }))
    },

    /**
     * 受理查分申请
     * 「更正成绩」会同步修改该次月考该科成绩，并写入修改记录（留痕）
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
        const record = this.scoreOf(appeal.studentId, appeal.examId, appeal.subjectId)
        if (record) {
          const oldScore = record.score
          record.score = Number(correctedScore)
          this.logs.unshift({
            logId: `LOG${Date.now()}`,
            studentId: record.studentId,
            examId: record.examId,
            subjectId: record.subjectId,
            oldScore,
            newScore: record.score,
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
