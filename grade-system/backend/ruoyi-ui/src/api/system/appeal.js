import request from '@/utils/request'

// 查询查分申请管理列表
export function listAppeal(query) {
  return request({
    url: '/system/appeal/list',
    method: 'get',
    params: query
  })
}

// 查询查分申请管理详细
export function getAppeal(appealId) {
  return request({
    url: '/system/appeal/' + appealId,
    method: 'get'
  })
}

// 新增查分申请管理
export function addAppeal(data) {
  return request({
    url: '/system/appeal',
    method: 'post',
    data: data
  })
}

// 修改查分申请管理
export function updateAppeal(data) {
  return request({
    url: '/system/appeal',
    method: 'put',
    data: data
  })
}

// 删除查分申请管理
export function delAppeal(appealId) {
  return request({
    url: '/system/appeal/' + appealId,
    method: 'delete'
  })
}
