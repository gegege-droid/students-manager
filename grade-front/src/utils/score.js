/**
 * 高中成绩计算与高考预测工具
 *
 * 与大学版的区别：没有学分、绩点、加权总评。
 * 高中关心的是：得分率 → 等级 → 班级/年级排名 → 用多次月考的趋势预测高考分数。
 */

// ==================== 基础 ====================

/** 得分率 = 分数 / 满分（0 ~ 1） */
export function scoreRate(score, fullScore) {
  if (!fullScore || score === null || score === undefined) return null
  return score / fullScore
}

/** 按得分率着色：优秀绿 / 良好绿 / 及格橙 / 待提高红 */
export function rateColor(rate) {
  if (rate === null || rate === undefined) return '#718096'
  if (rate >= 0.85) return '#38a169'
  if (rate >= 0.7) return '#2b6cb0'
  if (rate >= 0.6) return '#ed8936'
  return '#e53e3e'
}

/**
 * 等级 —— 高中成绩单的常见写法，按得分率划分
 * 返回 key / 中文 label / 配色
 */
export function gradeLevel(rate) {
  if (rate === null || rate === undefined) {
    return { key: 'NONE', label: '—', color: '#718096', bg: '#f2f4f7', border: '#e2e8f0' }
  }
  if (rate >= 0.85) return { key: 'A', label: '优秀', color: '#2f6b46', bg: '#eaf4ee', border: '#cfe6d8' }
  if (rate >= 0.7) return { key: 'B', label: '良好', color: '#2b6cb0', bg: '#ebf4ff', border: '#d3e0f0' }
  if (rate >= 0.6) return { key: 'C', label: '及格', color: '#a9702e', bg: '#fbf1e6', border: '#f0dfcd' }
  return { key: 'D', label: '待提高', color: '#a95050', bg: '#fdf2f2', border: '#eed6d6' }
}

// ==================== 排名 ====================

/**
 * 按某个数值降序排名，返回 { 值: 名次 } 的映射，同分同名次
 * @param {Array} rows 数据行
 * @param {string} key 参与排名的字段
 */
export function buildRankMap(rows, key) {
  const sorted = [...rows]
    .filter((row) => row[key] !== null && row[key] !== undefined)
    .sort((a, b) => b[key] - a[key])
  const map = {}
  let lastValue = null
  let lastRank = 0
  sorted.forEach((row, index) => {
    const value = row[key]
    if (lastValue !== null && value === lastValue) {
      map[`${row.studentId}`] = lastRank
    } else {
      map[`${row.studentId}`] = index + 1
      lastRank = index + 1
      lastValue = value
    }
  })
  return map
}

/** 标准正态分布累积函数（Abramowitz-Stegun 近似），用于估算年级排名 */
function normalCdf(z) {
  const t = 1 / (1 + 0.2316419 * Math.abs(z))
  const d = 0.3989423 * Math.exp((-z * z) / 2)
  const p =
    d *
    t *
    (0.3193815 + t * (-0.3565638 + t * (1.781478 + t * (-1.821256 + t * 1.330274))))
  return z > 0 ? 1 - p : p
}

/**
 * 估算年级排名。
 * 演示环境只有一个班的数据，无法真实排年级，因此按「全年级总分近似正态分布」折算。
 * 说明：这是示例算法，真实系统应由年级成绩库直接统计。
 */
export function estimateGradeRank(totalScore, totalFullScore, gradeStudentCount) {
  if (!totalFullScore || totalScore === null || totalScore === undefined) return null
  const rate = totalScore / totalFullScore
  const MEAN = 0.68 // 全年级平均得分率（示例）
  const SD = 0.115 // 标准差（示例）
  const percentile = normalCdf((rate - MEAN) / SD)
  return Math.max(1, Math.min(gradeStudentCount, Math.round(gradeStudentCount * (1 - percentile))))
}

// ==================== 趋势与预测 ====================

/**
 * 线性回归斜率（最小二乘）：返回「每前进一次考试，数值平均变化多少」
 * 少于 2 个点返回 0
 */
export function linearSlope(values) {
  const n = values.length
  if (n < 2) return 0
  const meanX = (n - 1) / 2
  const meanY = values.reduce((sum, value) => sum + value, 0) / n
  let numerator = 0
  let denominator = 0
  values.forEach((value, index) => {
    numerator += (index - meanX) * (value - meanY)
    denominator += (index - meanX) ** 2
  })
  return denominator ? numerator / denominator : 0
}

/** 预测参数（可调，页面上会展示这三个数字，答辩时好解释） */
export const PREDICT_CONFIG = {
  /** 距高考预计还有几次大考 */
  gap: 2,
  /** 趋势阻尼系数：只采信趋势外推的一半，避免过度预测 */
  damping: 0.5,
  /** 参与斜率计算的最近场次数 */
  recentCount: 3,
  /** 预测得分率的下限 / 上限 */
  minRate: 0.3,
  maxRate: 1.0
}

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value))
}

/**
 * 单科高考成绩预测 —— 「加权移动平均 + 趋势修正」
 *
 * 1. 把每次月考分数换算成得分率，这样不同满分（150 / 100）的科目可以放在一起比较
 * 2. 加权移动平均：第 i 次月考权重为 i，越近的考试权重越大
 * 3. 趋势修正：对最近 3 次得分率做最小二乘拟合，取斜率，外推 gap 次考试，再乘阻尼系数
 * 4. 预测得分率 × 高考满分 = 预测分；用历史得分率的标准差给出预测区间
 *
 * @param {Array} records [{ examId, score }] 按场次先后排序
 * @param {number} fullScore 该科高考满分
 */
export function predictSubject(records, fullScore, config = PREDICT_CONFIG) {
  const valid = (records || []).filter((item) => item.score !== null && item.score !== undefined)
  if (!valid.length) return null

  const rates = valid.map((item) => item.score / fullScore)
  const n = rates.length

  // ② 加权移动平均（权重 1,2,3… 越近越大）
  const weights = rates.map((_, index) => index + 1)
  const weightSum = weights.reduce((sum, value) => sum + value, 0)
  const baseRate = rates.reduce((sum, rate, index) => sum + rate * weights[index], 0) / weightSum

  // ③ 趋势修正
  const recent = rates.slice(-Math.min(config.recentCount, n))
  const slope = linearSlope(recent)
  const predictedRate = clamp(
    baseRate + slope * config.gap * config.damping,
    config.minRate,
    config.maxRate
  )

  // ④ 预测分与区间
  const mean = rates.reduce((sum, rate) => sum + rate, 0) / n
  const variance = rates.reduce((sum, rate) => sum + (rate - mean) ** 2, 0) / n
  const sd = Math.sqrt(variance)

  const predicted = Math.round(predictedRate * fullScore)
  const margin = Math.max(Math.round(1.5 * sd * fullScore), Math.round(fullScore * 0.02))

  return {
    fullScore,
    count: n,
    latest: valid[valid.length - 1].score,
    baseRate,
    predictedRate,
    slope,
    sd,
    predicted,
    low: clamp(predicted - margin, 0, fullScore),
    high: clamp(predicted + margin, 0, fullScore),
    /** 每次考试平均变化多少分（用于趋势列展示） */
    deltaPerExam: Number((slope * fullScore).toFixed(1)),
    confidence: confidenceOf(n, sd)
  }
}

/**
 * 置信度：场次越多、波动越小，越可信
 * @returns {'HIGH' | 'MEDIUM' | 'LOW'}
 */
export function confidenceOf(count, sd) {
  if (count >= 5 && sd <= 0.05) return 'HIGH'
  if (count >= 3 && sd <= 0.09) return 'MEDIUM'
  return 'LOW'
}

/** 把各科预测汇总成总分预测 */
export function sumPredictions(predictions) {
  const valid = predictions.filter(Boolean)
  return {
    predicted: valid.reduce((sum, item) => sum + item.predicted, 0),
    low: valid.reduce((sum, item) => sum + item.low, 0),
    high: valid.reduce((sum, item) => sum + item.high, 0),
    fullScore: valid.reduce((sum, item) => sum + item.fullScore, 0)
  }
}

/** 距高考还有多少天（已过则返回负数） */
export function daysUntil(dateText, now = new Date()) {
  const target = new Date(String(dateText).replace(/-/g, '/'))
  if (Number.isNaN(target.getTime())) return null
  const start = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const end = new Date(target.getFullYear(), target.getMonth(), target.getDate())
  return Math.round((end - start) / 86400000)
}
