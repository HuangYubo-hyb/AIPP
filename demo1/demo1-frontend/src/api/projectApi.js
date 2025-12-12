// 项目相关API接口封装
import request from '@/utils/request'

// 获取项目列表
export const getProjects = (params) => {
  return request.get('/projects', { params })
}

// 获取项目详情
export const getProjectById = (id) => {
  return request.get(`/projects/${id}`)
}

// 创建项目
export const createProject = (data) => {
  return request.post('/projects', data)
}

// 根据项目ID获取任务列表
export const getTasksByProjectId = (projectId) => {
  return request.get(`/projects/${projectId}/tasks`)
}