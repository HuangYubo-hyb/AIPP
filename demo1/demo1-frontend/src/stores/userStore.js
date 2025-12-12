import { defineStore } from 'pinia'
import { loginApi } from '@/api/authApi'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const userInfo = ref(null)
  
  // token
  const token = ref(localStorage.getItem('token') || '')
  
  // 是否已登录
  const isLoggedIn = ref(!!token.value)
  
  // 登录
  const login = async (loginData) => {
    try {
      const response = await loginApi(loginData)
      console.log('Login API response:', response);
      
      // 检查响应数据格式
      if (!response.data || !response.data.token) {
        throw new Error('登录响应格式不正确，缺少token')
      }
      
      const { token: accessToken, user } = response.data
      
      // 验证token是否有效
      if (!accessToken || accessToken.length < 10) {
        throw new Error('获取到的token无效')
      }
      
      // 保存token
      token.value = accessToken
      localStorage.setItem('token', accessToken)
      
      // 保存用户信息
      userInfo.value = user
      
      // 设置登录状态
      isLoggedIn.value = true
      
      return response
    } catch (error) {
      console.error('Login error in user store:', error);
      if (error.response && error.response.data) {
        throw new Error(error.response.data.message || '登录失败')
      } else {
        throw new Error(error.message || '登录失败')
      }
    }
  }
  
  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem('token')
    console.log('User logged out, token cleared')
  }
  
  // 初始化时从localStorage恢复token
  const initFromStorage = () => {
    const storedToken = localStorage.getItem('token')
    if (storedToken) {
      token.value = storedToken
      isLoggedIn.value = true
    }
  }
  
  // 在store创建时初始化
  initFromStorage()
  
  // 检查是否为管理员
  const isAdmin = () => {
    return userInfo.value && userInfo.value.role === 'ADMIN'
  }
  
  // 重新登录
  const refreshLogin = async () => {
    try {
      // 尝试重新获取token
      const response = await loginApi({
        username: userInfo.value.username,
        password: 'your_password_here' // 这里需要实际的密码
      })
      
      const { token: accessToken, user } = response.data
      
      // 保存新的token
      token.value = accessToken
      localStorage.setItem('token', accessToken)
      
      // 更新用户信息
      userInfo.value = user
      
      return response
    } catch (error) {
      console.error('Refresh login failed:', error)
      // 如果刷新失败，执行登出
      logout()
      throw error
    }
  }
  
  return {
    userInfo,
    token,
    isLoggedIn,
    login,
    logout,
    isAdmin,
    refreshLogin
  }
})
