import request from '@/utils/request'

// 查询成绩修改记录列表
export function listLog(query) {
  return request({
    url: '/system/log/list',
    method: 'get',
    params: query
  })
}

// 查询成绩修改记录详细
export function getLog(logId) {
  return request({
    url: '/system/log/' + logId,
    method: 'get'
  })
}

// 新增成绩修改记录
export function addLog(data) {
  return request({
    url: '/system/log',
    method: 'post',
    data: data
  })
}

// 修改成绩修改记录
export function updateLog(data) {
  return request({
    url: '/system/log',
    method: 'put',
    data: data
  })
}

// 删除成绩修改记录
export function delLog(logId) {
  return request({
    url: '/system/log/' + logId,
    method: 'delete'
  })
}
