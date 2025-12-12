import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProjectListView from '../views/ProjectListView.vue'
import ProjectTaskView from '../views/ProjectTaskView.vue'
import UserManagementView from '../views/UserManagementView.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView
  },
  {
    path: '/projects',
    name: 'Projects',
    component: ProjectListView,
    meta: { requiresAuth: true }
  },
  {
    path: '/user-management',
    name: 'UserManagement',
    component: UserManagementView,
    meta: { requiresAuth: true }
  },
  {
    path: '/projects/:projectId/tasks',
    name: 'ProjectTasks',
    component: ProjectTaskView,
    props: true,
    meta: { requiresAuth: true }
  },

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 如果路由需要认证但没有token，则重定向到登录页
  if (to.meta.requiresAuth && !token) {
    console.log('Route requires auth but no token found, redirecting to login')
    next('/login')
  } else if (to.meta.requiresAuth && token) {
    // 有token，允许访问
    console.log('Route requires auth and token found, allowing access')
    next()
  } else {
    // 不需要认证的路由，直接通过
    next()
  }
})

export default router