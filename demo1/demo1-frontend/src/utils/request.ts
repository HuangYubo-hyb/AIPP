import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 对响应数据做点什么
    return response
  },
  (error) => {
    // 对响应错误做点什么
    console.error('API Error:', error.response || error);
    
    if (!error.response) {
      // 网络错误
      ElMessage.error('网络连接错误，请检查您的网络连接');
      console.error('Network error: Please check your connection');
    } else if (error.response?.status === 401 || error.response?.status === 403) {
      // token过期或无效
      const currentPath = window.location.pathname;
      
      // 如果当前不在登录页或注册页，才需要跳转
      if (currentPath !== '/login' && currentPath !== '/register') {
        console.warn('Token invalid, redirecting to login');
        localStorage.removeItem('token');
        
        // 使用路由跳转而不是直接修改location，避免重复触发
        ElMessage.error('登录已过期，请重新登录');
        
        // 使用路由跳转，避免重复触发拦截器
        if (typeof window !== 'undefined' && window.location) {
          // 延迟跳转，让用户看到错误信息
          setTimeout(() => {
            // 使用replace避免在历史记录中留下记录
            window.location.replace('/login');
          }, 1500);
        }
      }
    } else if (error.response?.status >= 500) {
      ElMessage.error('服务器内部错误，请稍后再试');
    }
    return Promise.reject(error)
  }
)

export default request