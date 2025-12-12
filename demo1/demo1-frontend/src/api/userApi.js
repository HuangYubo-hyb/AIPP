import request from '@/utils/request'

/**
 * 获取用户列表
 * @returns {Promise}
 */
export const getUsersApi = () => {
  return request.get('/users')
}

/**
 * 获取非管理员用户列表（用于任务分配）
 * @returns {Promise}
 */
export const getNonAdminUsersApi = () => {
  return request.get('/users/non-admin')
}

/**
 * 更新用户信息
 * @param {Number} id 用户ID
 * @param {Object} data 用户数据
 * @returns {Promise}
 */
export const updateUserApi = (id, data) => {
  return request.put(`/users/${id}`, data)
}

/**
 * 删除用户
 * @param {Number} id 用户ID
 * @returns {Promise}
 */
export const deleteUserApi = (id) => {
  return request.delete(`/users/${id}`)
}