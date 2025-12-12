<template>
  <div class="user-management-view">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <el-icon :size="28" class="title-icon"><UserFilled /></el-icon>
          <h1>用户管理</h1>
        </div>
        <div class="header-actions">
          <el-button 
            @click="goBack"
            :icon="ArrowLeft"
          >
            返回
          </el-button>
          <el-button 
            @click="handleLogout"
            :icon="SwitchButton"
          >
            退出登录
          </el-button>
        </div>
      </div>
    </div>
    
    <el-card class="user-card" shadow="never">
      
      <!-- 用户表格 -->
      <el-table 
        :data="users" 
        style="width: 100%" 
        v-loading="loading"
        stripe
        class="user-table"
      >
        <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="username" label="用户名" min-width="150">
          <template #default="scope">
            <div class="username-cell">
              <el-icon class="user-icon"><User /></el-icon>
              <span>{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="200"></el-table-column>
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'info'" effect="dark">
              {{ scope.row.role === 'ADMIN' ? '管理员' : '普通员工' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="200" align="center"></el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              @click="editUser(scope.row)"
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteUser(scope.row)"
              :disabled="scope.row.role === 'ADMIN' && users.filter(u => u.role === 'ADMIN').length <= 1"
              :icon="Delete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="users.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无用户数据" />
      </div>

      <!-- 编辑用户对话框 -->
      <el-dialog v-model="showEditDialog" title="编辑用户" width="500px">
        <el-form 
          ref="userFormRef" 
          :model="currentUser" 
          label-width="80px"
        >
          <el-form-item label="用户名">
            <el-input v-model="currentUser.username" disabled></el-input>
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="currentUser.email"></el-input>
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="currentUser.role" style="width: 100%">
              <el-option label="普通员工" value="EMPLOYEE"></el-option>
              <el-option label="管理员" value="ADMIN"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="currentUser.password" type="password" placeholder="留空则不修改密码"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showEditDialog = false">取消</el-button>
            <el-button 
              type="primary" 
              @click="saveUser"
              :loading="saving"
            >
              保存
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElIcon } from 'element-plus'
import { UserFilled, ArrowLeft, SwitchButton, User, Edit, Delete } from '@element-plus/icons-vue'
import { getUsersApi, updateUserApi, deleteUserApi } from '@/api/userApi'
import { useUserStore } from '@/stores/userStore'

// 路由实例
const router = useRouter()

// 用户状态
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const users = ref([])
const showEditDialog = ref(false)
const userFormRef = ref()
const currentUser = ref({
  id: null,
  username: '',
  email: '',
  role: 'EMPLOYEE',
  password: ''
})

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const response = await getUsersApi()
    users.value = response.data
  } catch (error) {
    ElMessage.error('获取用户列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 编辑用户
const editUser = (user) => {
  currentUser.value = { ...user, password: '' }
  showEditDialog.value = true
}

// 保存用户
const saveUser = async () => {
  if (!userFormRef.value) return
  
  saving.value = true
  try {
    await updateUserApi(currentUser.value.id, currentUser.value)
    ElMessage.success('用户信息更新成功')
    showEditDialog.value = false
    await fetchUsers()
  } catch (error) {
    ElMessage.error('更新用户信息失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 删除用户
const deleteUser = (user) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${user.username}" 吗？此操作不可撤销！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deleteUserApi(user.id)
      ElMessage.success('用户删除成功')
      await fetchUsers()
    } catch (error) {
      ElMessage.error('删除用户失败: ' + error.message)
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 页面加载时获取数据
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management-view {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  color: #667eea;
}

.title-section h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.user-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.user-table {
  margin-top: 0;
}

.user-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.user-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.username-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-icon {
  color: #667eea;
  font-size: 16px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
}
</style>