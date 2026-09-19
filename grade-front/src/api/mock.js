/**
 * 演示数据 —— 与 docs/04-data-and-vue.md §2 的 Mock 数据完全一致。
 * 前端先用这套数据跑通全部交互；接真实后端时只需替换 api/request.js 的实现。
 *
 * 注意：成绩的 totalScore / gpa 不在这里写死，由 stores/score.js 按 §5.3 公式计算，
 *       避免「示例数据与公式不一致」这种低级问题。
 */

/** 登录账号 */
export const accounts = [
  {
    loginId: '2021001',
    password: '123456',
    role: 'STUDENT',
    name: '张三',
    classId: 'G2101',
    className: '高一(1)班'
  },
  {
    loginId: '2021003',
    password: '123456',
    role: 'STUDENT',
    name: '王五',
    classId: 'G2101',
    className: '高一(1)班'
  },
  {
    loginId: '2021005',
    password: '123456',
    role: 'STUDENT',
    name: '钱七',
    classId: 'G2101',
    className: '高一(1)班'
  },
  {
    loginId: 'T001',
    password: '123456',
    role: 'TEACHER',
    name: '李老师',
    classId: '',
    className: '',
    manageClassIds: ['G2101']
  }
]

/** 学期 */
export const terms = ['2024-2025-1']

/** 班级 */
export const classes = [{ classId: 'G2101', className: '高一(1)班' }]

/** 学生名单（班主任 P4 批量录入的对象） */
export const students = [
  { studentId: '2021001', name: '张三', classId: 'G2101', className: '高一(1)班' },
  { studentId: '2021002', name: '李四', classId: 'G2101', className: '高一(1)班' },
  { studentId: '2021003', name: '王五', classId: 'G2101', className: '高一(1)班' },
  { studentId: '2021004', name: '赵六', classId: 'G2101', className: '高一(1)班' },
  { studentId: '2021005', name: '钱七', classId: 'G2101', className: '高一(1)班' },
  { studentId: '2021006', name: '孙八', classId: 'G2101', className: '高一(1)班' }
]

/** 课程 */
export const courses = [
  { courseId: 'CHN101', courseName: '语文', credit: 5, term: '2024-2025-1', teacherId: 'T001', courseType: 'REQUIRED' },
  { courseId: 'MAT101', courseName: '数学', credit: 5, term: '2024-2025-1', teacherId: 'T001', courseType: 'REQUIRED' },
  { courseId: 'ENG101', courseName: '英语', credit: 5, term: '2024-2025-1', teacherId: 'T001', courseType: 'REQUIRED' },
  { courseId: 'PHY101', courseName: '物理', credit: 4, term: '2024-2025-1', teacherId: 'T001', courseType: 'REQUIRED' },
  { courseId: 'CHM101', courseName: '化学', credit: 4, term: '2024-2025-1', teacherId: 'T001', courseType: 'REQUIRED' },
  { courseId: 'BIO101', courseName: '生物', credit: 4, term: '2024-2025-1', teacherId: 'T001', courseType: 'REQUIRED' },
  { courseId: 'POL101', courseName: '政治', credit: 3, term: '2024-2025-1', teacherId: 'T001', courseType: 'REQUIRED' },
  { courseId: 'HIS101', courseName: '历史', credit: 3, term: '2024-2025-1', teacherId: 'T001', courseType: 'REQUIRED' },
  { courseId: 'GEO101', courseName: '地理', credit: 3, term: '2024-2025-1', teacherId: 'T001', courseType: 'REQUIRED' }
]

/** 成绩记录（不写 totalScore / gpa，由 store 计算） */
export const scores = [
  // 张三 —— 覆盖「优秀 / 良好 / 中等 / 不及格」四档，便于演示分数着色
  { scoreId: 'SC20250101001', studentId: '2021001', courseId: 'CHN101', term: '2024-2025-1', usualScore: 85, finalScore: 92, recorderId: 'T001', recordTime: '2025-01-10 16:20' },
  { scoreId: 'SC20250101002', studentId: '2021001', courseId: 'MAT101', term: '2024-2025-1', usualScore: 78, finalScore: 82, recorderId: 'T001', recordTime: '2025-01-10 16:22' },
  { scoreId: 'SC20250101003', studentId: '2021001', courseId: 'ENG101', term: '2024-2025-1', usualScore: 66, finalScore: 72, recorderId: 'T001', recordTime: '2025-01-10 16:24' },
  { scoreId: 'SC20250101004', studentId: '2021001', courseId: 'PHY101', term: '2024-2025-1', usualScore: 58, finalScore: 64, recorderId: 'T001', recordTime: '2025-01-10 16:26' },
  { scoreId: 'SC20250101005', studentId: '2021001', courseId: 'CHM101', term: '2024-2025-1', usualScore: 91, finalScore: 95, recorderId: 'T001', recordTime: '2025-01-10 16:28' },
  { scoreId: 'SC20250101006', studentId: '2021001', courseId: 'BIO101', term: '2024-2025-1', usualScore: 88, finalScore: 86, recorderId: 'T001', recordTime: '2025-01-10 16:30' },
  { scoreId: 'SC20250101007', studentId: '2021001', courseId: 'POL101', term: '2024-2025-1', usualScore: 95, finalScore: 90, recorderId: 'T001', recordTime: '2025-01-10 16:32' },
  { scoreId: 'SC20250101008', studentId: '2021001', courseId: 'HIS101', term: '2024-2025-1', usualScore: 52, finalScore: 48, recorderId: 'T001', recordTime: '2025-01-10 16:34' },
  { scoreId: 'SC20250101009', studentId: '2021001', courseId: 'GEO101', term: '2024-2025-1', usualScore: 82, finalScore: 88, recorderId: 'T001', recordTime: '2025-01-10 16:36' },

  // 王五
  { scoreId: 'SC20250101011', studentId: '2021003', courseId: 'CHN101', term: '2024-2025-1', usualScore: 76, finalScore: 81, recorderId: 'T001', recordTime: '2025-01-10 16:40' },
  { scoreId: 'SC20250101012', studentId: '2021003', courseId: 'ENG101', term: '2024-2025-1', usualScore: 66, finalScore: 72, recorderId: 'T001', recordTime: '2025-01-10 16:42' },

  // 钱七
  { scoreId: 'SC20250101021', studentId: '2021005', courseId: 'PHY101', term: '2024-2025-1', usualScore: 58, finalScore: 64, recorderId: 'T001', recordTime: '2025-01-10 16:46' }
]

/** 查分申请 */
export const appeals = [
  {
    appealId: 'AP20250115001',
    studentId: '2021001',
    courseId: 'HIS101',
    term: '2024-2025-1',
    originalScore: 50,
    reason: '平时分已按要求提交全部作业，请老师复核登分情况',
    applyTime: '2025-01-15 14:30',
    status: 'PENDING',
    result: '',
    correctedScore: null,
    opinion: '',
    handlerId: '',
    handleTime: ''
  },
  {
    appealId: 'AP20250115002',
    studentId: '2021003',
    courseId: 'ENG101',
    term: '2024-2025-1',
    originalScore: 70,
    reason: '期末卷面估分 78，与公布成绩差距较大',
    applyTime: '2025-01-15 15:10',
    status: 'PENDING',
    result: '',
    correctedScore: null,
    opinion: '',
    handlerId: '',
    handleTime: ''
  },
  {
    appealId: 'AP20250115003',
    studentId: '2021005',
    courseId: 'PHY101',
    term: '2024-2025-1',
    originalScore: 62,
    reason: '平时分一项显示为空，疑似漏录',
    applyTime: '2025-01-15 18:45',
    status: 'PENDING',
    result: '',
    correctedScore: null,
    opinion: '',
    handlerId: '',
    handleTime: ''
  },
  {
    appealId: 'AP20241220003',
    studentId: '2021001',
    courseId: 'CHN101',
    term: '2024-2025-1',
    originalScore: 89,
    reason: '期末成绩与预期差距较大',
    applyTime: '2024-12-20 09:15',
    status: 'ACCEPTED',
    result: 'KEEP',
    correctedScore: null,
    opinion: '已调阅答卷逐题复核，计分无误，维持原成绩。',
    handlerId: 'T001',
    handleTime: '2024-12-21 09:00'
  },
  {
    appealId: 'AP20241218001',
    studentId: '2021001',
    courseId: 'BIO101',
    term: '2024-2025-1',
    originalScore: 87,
    reason: '听力部分疑似未计入总分',
    applyTime: '2024-12-18 16:40',
    status: 'REJECTED',
    result: '',
    correctedScore: null,
    opinion: '听力成绩已计入，复核后确认计分无误，驳回申请。',
    handlerId: 'T001',
    handleTime: '2024-12-19 10:10'
  }
]

/** 成绩修改记录（留痕） */
export const changeLogs = [
  {
    logId: 'LOG20250116001',
    scoreId: 'SC20250101004',
    studentId: '2021001',
    courseId: 'PHY101',
    term: '2024-2025-1',
    oldUsual: 58,
    oldFinal: 60,
    oldTotal: 59,
    newUsual: 58,
    newFinal: 64,
    newTotal: 62,
    changeReason: '登分时看错行',
    operatorId: 'T001',
    operateTime: '2025-01-08 15:05',
    source: 'MANUAL'
  },
  {
    logId: 'LOG20241221001',
    scoreId: 'SC20250101001',
    studentId: '2021001',
    courseId: 'CHN101',
    term: '2024-2025-1',
    oldUsual: 80,
    oldFinal: 92,
    oldTotal: 87,
    newUsual: 85,
    newFinal: 92,
    newTotal: 89,
    changeReason: '平时分漏计一次课堂作业，据实更正',
    operatorId: 'T001',
    operateTime: '2024-12-21 09:40',
    source: 'APPEAL'
  }
]
