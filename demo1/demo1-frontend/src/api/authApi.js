import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data 登录数据 {username, password}
 * @returns {Promise}
 */
export const loginApi = (data) => {
  return request.post('/login', data)
}

/**
 * 获取验证码
 * @returns {Promise}
 */
export const getCaptchaApi = () => {
  return request.get('/captcha')
}

/**
 * 用户注册
 * @param {Object} data 注册数据 {username, password, email, captcha}
 * @returns {Promise}
 */
export const registerApi = (data) => {
  return request.post('/register', data)
}

/**
 * 获取用户列表
 * @returns {Promise}
 */
export const getUsersApi = () => {
  return request.get('/users')
}