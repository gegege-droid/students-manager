import request from '@/utils/request'

// 查询班级管理列表
export function listClassroom(query) {
  return request({
    url: '/system/classroom/list',
    method: 'get',
    params: query
  })
}

// 查询班级管理详细
export function getClassroom(classId) {
  return request({
    url: '/system/classroom/' + classId,
    method: 'get'
  })
}

// 新增班级管理
export function addClassroom(data) {
  return request({
    url: '/system/classroom',
    method: 'post',
    data: data
  })
}

// 修改班级管理
export function updateClassroom(data) {
  return request({
    url: '/system/classroom',
    method: 'put',
    data: data
  })
}

// 删除班级管理
export function delClassroom(classId) {
  return request({
    url: '/system/classroom/' + classId,
    method: 'delete'
  })
}
