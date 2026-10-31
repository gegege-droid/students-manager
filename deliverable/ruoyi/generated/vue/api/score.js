import request from '@/utils/request'

// 查询月考成绩列表
export function listScore(query) {
  return request({
    url: '/business/score/list',
    method: 'get',
    params: query
  })
}

// 查询月考成绩详细
export function getScore(scoreId) {
  return request({
    url: '/business/score/' + scoreId,
    method: 'get'
  })
}

// 新增月考成绩
export function addScore(data) {
  return request({
    url: '/business/score',
    method: 'post',
    data: data
  })
}

// 修改月考成绩
export function updateScore(data) {
  return request({
    url: '/business/score',
    method: 'put',
    data: data
  })
}

// 删除月考成绩
export function delScore(scoreId) {
  return request({
    url: '/business/score/' + scoreId,
    method: 'delete'
  })
}
