/**
 * 成绩计算工具 —— 对应需求规格说明书 §5.3 公式
 * 前端与 Axure 原型使用同一套规则，保证原型与实现一致。
 */

/** 总评 = round(平时 × 0.4 + 期末 × 0.6)；任一项为空则返回 null */
export function calcTotal(usualScore, finalScore) {
  if (usualScore === null || usualScore === undefined || usualScore === '') return null
  if (finalScore === null || finalScore === undefined || finalScore === '') return null
  return Math.round(Number(usualScore) * 0.4 + Number(finalScore) * 0.6)
}

/** 绩点 = 总评 < 60 ? 0 : (总评 - 50) / 10，保留 1 位小数 */
export function calcGpa(totalScore) {
  if (totalScore === null || totalScore === undefined) return null
  return totalScore < 60 ? 0 : Number(((totalScore - 50) / 10).toFixed(1))
}

/** 分数颜色：>= 80 绿，60 ~ 79 橙，< 60 红 */
export function scoreColor(totalScore) {
  if (totalScore === null || totalScore === undefined) return '#718096'
  if (totalScore >= 80) return '#38a169'
  if (totalScore >= 60) return '#ed8936'
  return '#e53e3e'
}

/** 分数等级文案 */
export function scoreLevel(totalScore) {
  if (totalScore === null || totalScore === undefined) return '—'
  if (totalScore >= 90) return '优秀'
  if (totalScore >= 80) return '良好'
  if (totalScore >= 70) return '中等'
  if (totalScore >= 60) return '及格'
  return '不及格'
}

/** 平均分（保留 1 位小数） */
export function calcAvgScore(scores) {
  const list = scores.filter((s) => s.totalScore !== null && s.totalScore !== undefined)
  if (!list.length) return 0
  return Number((list.reduce((sum, s) => sum + s.totalScore, 0) / list.length).toFixed(1))
}

/** 学分绩点 = Σ(绩点 × 学分) / Σ学分，保留 2 位小数 */
export function calcAvgGpa(scores) {
  const list = scores.filter((s) => s.gpa !== null && s.gpa !== undefined && s.credit)
  const creditSum = list.reduce((sum, s) => sum + s.credit, 0)
  if (!creditSum) return 0
  return Number((list.reduce((sum, s) => sum + s.gpa * s.credit, 0) / creditSum).toFixed(2))
}
