/**
 * 演示数据 —— 高中版（新高考「3+1+2」模式）
 *
 * 与大学版的区别：
 *  - 学号 → 高中学生学号；课程 → 高中科目；学期 → 月考场次
 *  - 取消学分 / 绩点：新高考没有这两样，改为「分数 + 得分率 + 等级 + 班级/年级排名」
 *  - 满分不同：语数外各 150，其余各 100，总分 750
 *  - 新增：多次月考成绩序列（用于趋势分析）与高考成绩预测
 *
 * 成绩不手写，用「确定性伪随机」按 学生能力 + 科目偏置 + 个人趋势 + 噪声 生成，
 * 保证每次刷新数据完全一致（seed 固定），同时有真实感：有人稳步上升、有人在下滑。
 */

/** 科目 —— 新高考 3+1+2：3 门必考 + 1 门首选(物理/历史) + 2 门再选 */
export const SUBJECTS = [
  { subjectId: 'CHINESE', name: '语文', fullScore: 150, category: 'REQUIRED' },
  { subjectId: 'MATH', name: '数学', fullScore: 150, category: 'REQUIRED' },
  { subjectId: 'ENGLISH', name: '外语', fullScore: 150, category: 'REQUIRED' },
  { subjectId: 'PHYSICS', name: '物理', fullScore: 100, category: 'PRIMARY' },
  { subjectId: 'HISTORY', name: '历史', fullScore: 100, category: 'PRIMARY' },
  { subjectId: 'CHEMISTRY', name: '化学', fullScore: 100, category: 'SECONDARY' },
  { subjectId: 'BIOLOGY', name: '生物', fullScore: 100, category: 'SECONDARY' },
  { subjectId: 'POLITICS', name: '思想政治', fullScore: 100, category: 'SECONDARY' },
  { subjectId: 'GEOGRAPHY', name: '地理', fullScore: 100, category: 'SECONDARY' }
]

/** 月考场次 —— 高三一整学年。E07 标记 pending：刚考完、成绩尚未录入，用来演示「录入」页 */
export const EXAM_SESSIONS = [
  { examId: 'E01', name: '第一次月考', date: '2026-09-05', stage: '高三上学期' },
  { examId: 'E02', name: '第二次月考', date: '2026-10-10', stage: '高三上学期' },
  { examId: 'E03', name: '第三次月考', date: '2026-11-07', stage: '高三上学期' },
  { examId: 'E04', name: '第四次月考', date: '2026-12-05', stage: '高三上学期' },
  { examId: 'E05', name: '第五次月考', date: '2027-03-13', stage: '高三下学期' },
  { examId: 'E06', name: '第六次月考', date: '2027-04-10', stage: '高三下学期' },
  { examId: 'E07', name: '第七次月考', date: '2027-05-08', stage: '高三下学期', pending: true }
]

/** 已出成绩的场次（趋势分析、预测、排名都只用这些） */
export const SCORED_SESSIONS = EXAM_SESSIONS.filter((item) => !item.pending)

/** 年级 / 班级 / 高考信息 */
export const GRADE_INFO = {
  gradeName: '2024级高三',
  classId: 'G3-2',
  className: '高三(2)班',
  gradeStudentCount: 480,
  examDate: '2027-06-07',
  /** 各科高考满分表（3+1+2）：语文/数学/外语各 150，首选与再选各 100 */
  totalFullScore: 750
}

/**
 * 高考参考分数线（示例值，并非任何省份真实数据，答辩时如实说明即可）
 * 新高考 3+1+2 省份通常只划「本科线」和「特殊类型招生控制线」。
 */
export const REFERENCE_LINES = [
  { key: 'SPECIAL', label: '特殊类型招生控制线', score: 520, color: '#2b6cb0' },
  { key: 'UNDERGRAD', label: '本科线', score: 430, color: '#38a169' }
]

/** 三门必考科目 —— 所有学生都要考 */
const REQUIRED_SUBJECT_IDS = ['CHINESE', 'MATH', 'ENGLISH']

/**
 * 学生名单（一个班 6 人，含 3 种选科组合）
 *  electives : 选考科目（1 门首选 + 2 门再选）
 *  subjects  : 实际参加考试的 6 门 = 3 门必考 + electives
 *  base      : 基础得分率水平
 *  trend     : 每次月考得分率的变化量（正数 = 在进步）
 */
export const STUDENTS = [
  { studentId: '20230101', name: '林晓', classId: 'G3-2', className: '高三(2)班', combination: '物化生', electives: ['PHYSICS', 'CHEMISTRY', 'BIOLOGY'], subjects: [...REQUIRED_SUBJECT_IDS, 'PHYSICS', 'CHEMISTRY', 'BIOLOGY'], base: 0.86, trend: 0.006 },
  { studentId: '20230102', name: '陈宇航', classId: 'G3-2', className: '高三(2)班', combination: '物化生', electives: ['PHYSICS', 'CHEMISTRY', 'BIOLOGY'], subjects: [...REQUIRED_SUBJECT_IDS, 'PHYSICS', 'CHEMISTRY', 'BIOLOGY'], base: 0.79, trend: 0.002 },
  { studentId: '20230103', name: '苏雨桐', classId: 'G3-2', className: '高三(2)班', combination: '物化生', electives: ['PHYSICS', 'CHEMISTRY', 'BIOLOGY'], subjects: [...REQUIRED_SUBJECT_IDS, 'PHYSICS', 'CHEMISTRY', 'BIOLOGY'], base: 0.75, trend: -0.004 },
  { studentId: '20230104', name: '周子墨', classId: 'G3-2', className: '高三(2)班', combination: '物生地', electives: ['PHYSICS', 'BIOLOGY', 'GEOGRAPHY'], subjects: [...REQUIRED_SUBJECT_IDS, 'PHYSICS', 'BIOLOGY', 'GEOGRAPHY'], base: 0.78, trend: 0.004 },
  { studentId: '20230105', name: '郑一诺', classId: 'G3-2', className: '高三(2)班', combination: '史政地', electives: ['HISTORY', 'POLITICS', 'GEOGRAPHY'], subjects: [...REQUIRED_SUBJECT_IDS, 'HISTORY', 'POLITICS', 'GEOGRAPHY'], base: 0.82, trend: 0.001 },
  { studentId: '20230106', name: '顾清和', classId: 'G3-2', className: '高三(2)班', combination: '史政地', electives: ['HISTORY', 'POLITICS', 'GEOGRAPHY'], subjects: [...REQUIRED_SUBJECT_IDS, 'HISTORY', 'POLITICS', 'GEOGRAPHY'], base: 0.69, trend: 0.009 }
]

/** 登录账号（密码都是 123456） */
export const accounts = [
  { loginId: '20230101', password: '123456', role: 'STUDENT', name: '林晓', classId: 'G3-2', className: '高三(2)班' },
  { loginId: '20230103', password: '123456', role: 'STUDENT', name: '苏雨桐', classId: 'G3-2', className: '高三(2)班' },
  { loginId: '20230106', password: '123456', role: 'STUDENT', name: '顾清和', classId: 'G3-2', className: '高三(2)班' },
  { loginId: 'T001', password: '123456', role: 'TEACHER', name: '王老师', classId: '', className: '', manageClassIds: ['G3-2'] }
]

// ==================== 成绩生成 ====================

/** 确定性伪随机（mulberry32），同一 seed 永远产出同一序列 */
function mulberry32(seed) {
  let a = seed >>> 0
  return function next() {
    a = (a + 0x6d2b79f5) | 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

/** 字符串 → 32 位整数种子（FNV-1a） */
function hashSeed(text) {
  let h = 2166136261
  for (let i = 0; i < text.length; i += 1) {
    h ^= text.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return h >>> 0
}

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value))
}

const SUBJECT_MAP = Object.fromEntries(SUBJECTS.map((item) => [item.subjectId, item]))

/**
 * 生成全部成绩记录：学生 × 场次 × 该生选考的 6 门科目
 * 每条记录：{ studentId, examId, subjectId, score }
 */
function buildScores() {
  const list = []
  STUDENTS.forEach((student) => {
    student.subjects.forEach((subjectId) => {
      const subject = SUBJECT_MAP[subjectId]
      const rnd = mulberry32(hashSeed(`${student.studentId}|${subjectId}`))
      // 固定的科目偏置：-5% ~ +5%，让每个人有明显的强弱科
      const subjectBias = (rnd() - 0.5) * 0.1
      // 只为「已出成绩」的场次生成数据，pending 场次留给录入页演示
      SCORED_SESSIONS.forEach((session, index) => {
        const noise = (rnd() - 0.5) * 0.07
        const rate = clamp(student.base + subjectBias + student.trend * index + noise, 0.35, 0.99)
        list.push({
          scoreId: `SC${student.studentId}${session.examId}${subjectId}`,
          studentId: student.studentId,
          examId: session.examId,
          subjectId,
          score: Math.round(rate * subject.fullScore),
          recorderId: 'T001'
        })
      })
    })
  })
  return list
}

export const scores = buildScores()

/** 查分申请：某学生 · 某次月考 · 某科目 */
export const appeals = [
  {
    appealId: 'AP20260410001',
    studentId: '20230101',
    examId: 'E06',
    subjectId: 'MATH',
    originalScore: 121,
    reason: '最后一道解答题我写了完整过程，但公布分数比预期低了十几分，申请复核该题计分。',
    applyTime: '2027-04-12 14:30',
    status: 'PENDING',
    result: '',
    correctedScore: null,
    opinion: '',
    handlerId: '',
    handleTime: ''
  },
  {
    appealId: 'AP20260410002',
    studentId: '20230103',
    examId: 'E06',
    subjectId: 'PHYSICS',
    originalScore: 62,
    reason: '物理实验题第二问我写了两个正确步骤，估计不应该只得这些分，请老师复核。',
    applyTime: '2027-04-12 16:05',
    status: 'PENDING',
    result: '',
    correctedScore: null,
    opinion: '',
    handlerId: '',
    handleTime: ''
  },
  {
    appealId: 'AP20260314001',
    studentId: '20230106',
    examId: 'E05',
    subjectId: 'HISTORY',
    originalScore: 74,
    reason: '历史材料题答案与参考答案要点基本一致，怀疑阅卷时漏加要点分，申请查分。',
    applyTime: '2027-03-15 09:20',
    status: 'PENDING',
    result: '',
    correctedScore: null,
    opinion: '',
    handlerId: '',
    handleTime: ''
  },
  {
    appealId: 'AP20251206001',
    studentId: '20230102',
    examId: 'E04',
    subjectId: 'CHEMISTRY',
    originalScore: 78,
    reason: '化学选择题第五题我选了 C，公布答案是 B，但题目条件似乎支持 C，请求复核。',
    applyTime: '2025-12-06 15:40',
    status: 'ACCEPTED',
    result: 'KEEP',
    correctedScore: null,
    opinion: '已调阅原卷与标准答案逐题复核，第五题题干条件明确指向 B，计分无误，维持原成绩。',
    handlerId: 'T001',
    handleTime: '2025-12-07 10:15'
  },
  {
    appealId: 'AP20251108001',
    studentId: '20230105',
    examId: 'E03',
    subjectId: 'POLITICS',
    originalScore: 81,
    reason: '政治主观题得分明显低于平时水平，怀疑分数录入有误，申请核查。',
    applyTime: '2025-11-08 11:30',
    status: 'REJECTED',
    result: '',
    correctedScore: null,
    opinion: '经核查登分表与答题卡，分数录入无误；主观题失分点为未结合材料分析，驳回申请。',
    handlerId: 'T001',
    handleTime: '2025-11-09 09:00'
  }
]

/** 成绩修改记录（留痕） */
export const changeLogs = [
  {
    logId: 'LOG20251206001',
    studentId: '20230103',
    examId: 'E04',
    subjectId: 'PHYSICS',
    oldScore: 55,
    newScore: 62,
    changeReason: '登分时看错行，将 62 录成了 55，据实更正',
    operatorId: 'T001',
    operateTime: '2025-12-06 16:20',
    source: 'MANUAL'
  },
  {
    logId: 'LOG20251109001',
    studentId: '20230104',
    examId: 'E03',
    subjectId: 'BIOLOGY',
    oldScore: 71,
    newScore: 76,
    changeReason: '查分受理更正：经复核实验设计题第二问应得满分，已更正',
    operatorId: 'T001',
    operateTime: '2025-11-09 14:05',
    source: 'APPEAL'
  }
]

export const ALL_SUBJECT_IDS = SUBJECTS.map((item) => item.subjectId)
