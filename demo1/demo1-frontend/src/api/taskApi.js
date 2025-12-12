// 任务相关API接口封装
import request from '@/utils/request'

// 根据项目ID获取任务列表
export const getTasksByProjectId = (projectId) => {
  return request.get(`/tasks/project/${projectId}`)
}

// 创建任务
export const createTask = (data) => {
  return request.post('/tasks', data)
}

// 删除任务
export const deleteTask = (taskId) => {
  return request.delete(`/tasks/${taskId}`)
}

// 更新任务状态
export const updateTaskStatus = (taskId, status) => {
  return request.put(`/tasks/${taskId}/status?status=${status}`)
}

// 获取AI任务建议
export const getAiTaskSuggestions = (description) => {
  return request.post(`/tasks/ai-suggestions`, description)
}