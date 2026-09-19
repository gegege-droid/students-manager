/**
 * 表单校验规则 —— 对应需求规格说明书 §3 校验规则汇总表
 * 每个函数返回错误文案；返回空字符串表示通过。
 */

/** 账号：6~12 位字母或数字 */
export function validateAccount(value) {
  if (!value) return '请输入学号或工号'
  if (!/^[A-Za-z0-9]{6,12}$/.test(value)) return '请输入学号或工号（6~12 位字母或数字）'
  return ''
}

/** 密码：6~20 位 */
export function validatePassword(value) {
  if (!value) return '请输入密码'
  if (value.length < 6 || value.length > 20) return '请输入密码（6~20 位）'
  return ''
}

/** 验证码：4 位 */
export function validateCaptcha(value) {
  if (!value) return '请输入验证码'
  if (value.length !== 4) return '请输入 4 位验证码'
  return ''
}

/** 成绩：0~100 整数 */
export function isScoreValue(value) {
  if (value === null || value === undefined || value === '') return false
  const text = String(value).trim()
  if (!/^\d{1,3}$/.test(text)) return false
  const num = Number(text)
  return num >= 0 && num <= 100
}

export function validateScore(value, required = true) {
  if (value === null || value === undefined || value === '') {
    return required ? '成绩必须是 0~100 的整数' : ''
  }
  return isScoreValue(value) ? '' : '成绩必须是 0~100 的整数'
}

/** 申请理由：去首尾空格后 10~200 字 */
export function validateAppealReason(value) {
  const text = (value || '').trim()
  if (!text) return '请填写申请理由'
  if (text.length < 10 || text.length > 200) return '请填写 10~200 字的申请理由'
  return ''
}

/** 修改原因：5~100 字 */
export function validateChangeReason(value) {
  const text = (value || '').trim()
  if (!text) return '请填写修改原因'
  if (text.length < 5 || text.length > 100) return '请填写修改原因（5~100 字）'
  return ''
}

/** 处理意见：5~200 字 */
export function validateOpinion(value) {
  const text = (value || '').trim()
  if (!text) return '请填写处理意见'
  if (text.length < 5 || text.length > 200) return '请填写处理意见（5~200 字）'
  return ''
}
