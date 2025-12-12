<template>
  <div class="project-task-view">
    <div class="page-header">
      <div class="header-left">
        <el-button 
          circle
          @click="goBack"
          :icon="ArrowLeft"
          class="back-button"
        />
        <div class="header-title">
          <h2>{{ project?.name || '加载中...' }}</h2>
          <p class="subtitle">任务管理</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button 
          type="primary" 
          @click="showCreateTaskDialog = true"
          :icon="Plus"
        >
          创建任务
        </el-button>
        <el-button 
          @click="handleLogout"
          :icon="SwitchButton"
        >
          退出登录
        </el-button>
      </div>
    </div>

    <el-card class="task-card" shadow="never">
      <div class="view-toggle">
        <el-radio-group v-model="viewMode" size="default">
          <el-radio-button label="list">
            <el-icon><List /></el-icon>
            <span>列表视图</span>
          </el-radio-button>
          <el-radio-button label="board">
            <el-icon><Grid /></el-icon>
            <span>看板视图</span>
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- 列表视图 -->
      <div v-show="viewMode === 'list'" class="list-view">
        <el-table :data="allTasks" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="assignee" label="负责人" width="120" />
          <el-table-column prop="creator" label="创建人" width="120" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-dropdown @command="handleStatusChange">
                <el-button size="small" :disabled="!canUpdateTaskStatus(scope.row)">
                  修改状态<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item 
                      v-for="status in taskStatuses" 
                      :key="status.key" 
                      :command="{ taskId: scope.row.id, status: status.key }"
                      :disabled="scope.row.status === status.key"
                    >
                      {{ status.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button 
                size="small" 
                type="danger" 
                @click="handleDeleteTask(scope.row)"
                style="margin-left: 5px;"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 看板视图 -->
      <div v-show="viewMode === 'board'" class="board-view">
        <div class="kanban-container">
          <div 
            v-for="status in taskStatuses" 
            :key="status.key" 
            class="kanban-column"
            :class="`column-${status.key.toLowerCase()}`"
          >
            <div class="column-header">
              <div class="header-content">
                <h3>{{ status.label }}</h3>
                <el-badge :value="getTasksByStatus(status.key).length" class="task-badge" />
              </div>
            </div>
            
            <draggable 
              :list="getTasksByStatus(status.key)" 
              group="tasks" 
              item-key="id"
              @end="onTaskDrop"
              class="task-list"
              :animation="200"
              :disabled="!canDragTask()"
            >
              <template #item="{ element }">
                <div class="task-card">
                  <h4>{{ element.title }}</h4>
                  <p>{{ element.description }}</p>
                  <div class="task-footer">
                    <div>
                      <el-tag size="small" v-if="element.assignee">负责人: {{ element.assignee }}</el-tag>
                      <el-tag size="small" v-if="element.creator" type="info" style="margin-left: 5px;">创建人: {{ element.creator }}</el-tag>
                    </div>
                    <el-button 
                      size="small" 
                      type="danger" 
                      @click.stop="handleDeleteTask(element)"
                      style="margin-left: 5px;"
                      :disabled="!canDeleteTask(element)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </template>
            </draggable>
          </div>
        </div>
      </div>
      
      <!-- 创建任务对话框 -->
      <el-dialog v-model="showCreateTaskDialog" title="发布任务" width="500px">
        <el-form 
          ref="taskFormRef" 
          :model="taskForm" 
          :rules="taskFormRules"
          label-width="80px"
        >
          <el-form-item label="任务标题" prop="title">
            <el-input v-model="taskForm.title" />
          </el-form-item>
          <el-form-item label="任务描述" prop="description">
            <el-input v-model="taskForm.description" type="textarea" />
          </el-form-item>
          <el-form-item label="负责人" prop="assigneeId">
            <el-select v-model="taskForm.assigneeId" placeholder="请选择负责人" style="width: 100%">
              <el-option 
                v-for="user in users" 
                :key="user.id" 
                :label="user.username" 
                :value="user.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showCreateTaskDialog = false">取消</el-button>
            <el-button 
              type="primary" 
              @click="createTask"
              :loading="creatingTask"
            >
              确认发布
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import draggable from 'vuedraggable'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, SwitchButton, List, Grid } from '@element-plus/icons-vue'
import { getTasksByProjectId as taskApiGetTasksByProjectId, updateTaskStatus, createTask as apiCreateTask, deleteTask as apiDeleteTask } from '@/api/taskApi'
import { getProjectById as projectApiGetProjectById } from '@/api/projectApi'
import { getNonAdminUsersApi } from '@/api/userApi'
import { useUserStore } from '@/stores/userStore'

// 路由和路由器实例
const route = useRoute()
const router = useRouter()

// 用户状态
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const viewMode = ref('board') // 默认显示看板视图
const tasks = ref([])
const project = ref(null)
const users = ref([])

// 创建任务相关
const showCreateTaskDialog = ref(false)
const taskFormRef = ref()
const creatingTask = ref(false)

const taskForm = reactive({
  title: '',
  description: '',
  assigneeId: null,
  projectId: null
})

const taskFormRules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' }
  ],
  assigneeId: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ]
}

// 任务状态定义
const taskStatuses = [
  { key: 'TODO', label: '待办' },
  { key: 'DOING', label: '进行中' },
  { key: 'DONE', label: '已完成' }
]

// 获取当前项目名称
const fetchProjectName = async () => {
  try {
    // 修复：使用正确的API端点获取项目信息
    const response = await projectApiGetProjectById(route.params.projectId)
    project.value = response.data
    // 设置任务表单中的项目ID
    taskForm.projectId = project.value.id
  } catch (error) {
    console.error('获取项目信息失败:', error)
    // 401/403错误由响应拦截器统一处理，这里不再重复处理
    if (error.response?.status !== 401 && error.response?.status !== 403) {
      ElMessage.error('获取项目信息失败: ' + (error.response?.data?.message || error.message || '未知错误'))
    }
    project.value = null
  }
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    const response = await getNonAdminUsersApi()
    users.value = response.data
  } catch (error) {
    ElMessage.error('获取用户列表失败: ' + error.message)
    console.error('获取用户列表失败:', error)
  }
}

// 获取任务列表
const fetchTasks = async () => {
  loading.value = true
  try {
    console.log('Fetching tasks for project:', route.params.projectId);
    // 修复：使用正确的API端点获取任务列表
    const response = await taskApiGetTasksByProjectId(route.params.projectId)
    console.log('Tasks response:', response);
    tasks.value = response.data
  } catch (error) {
    console.error('获取任务列表失败:', error)
    // 401/403错误由响应拦截器统一处理，这里不再重复处理
    if (error.response?.status !== 401 && error.response?.status !== 403) {
      ElMessage.error('获取任务列表失败: ' + (error.response?.data?.message || error.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

// 根据状态获取任务
const getTasksByStatus = (status) => {
  return tasks.value.filter(task => task.status === status)
}

// 所有任务（用于列表视图）
const allTasks = computed(() => {
  return tasks.value
})

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'TODO': return 'info'
    case 'DOING': return 'warning'
    case 'DONE': return 'success'
    default: return 'info'
  }
}

// 获取状态标签文本
const getStatusLabel = (status) => {
  const statusObj = taskStatuses.find(s => s.key === status)
  return statusObj ? statusObj.label : status
}

// 处理状态变更
const handleStatusChange = async ({ taskId, status }) => {
  try {
    const response = await updateTaskStatus(taskId, status)
    
    if (response.data === true) {
      // 更新本地数据
      const task = tasks.value.find(t => t.id === taskId)
      if (task) {
        task.status = status
      }
      
      ElMessage.success('任务状态更新成功')
    } else {
      ElMessage.error('更新任务状态失败: 权限不足')
      // 恢复数据
      await fetchTasks()
    }
  } catch (error) {
    ElMessage.error('更新任务状态失败: ' + (error.message || '未知错误'))
    console.error('更新任务状态失败:', error)
    // 恢复数据
    await fetchTasks()
  }
}

// 任务拖拽结束事件
const onTaskDrop = async (event) => {
  try {
    const { item, to } = event
    const taskId = parseInt(item.dataset.id)
    const newStatus = to.parentElement.querySelector('.column-header h3').textContent
    
    // 根据列标题找到对应的状态键
    let statusKey = ''
    switch (newStatus) {
      case '待办':
        statusKey = 'TODO'
        break
      case '进行中':
        statusKey = 'DOING'
        break
      case '已完成':
        statusKey = 'DONE'
        break
      default:
        statusKey = 'TODO'
    }
    
    // 调用API更新任务状态
    const response = await updateTaskStatus(taskId, statusKey)
    
    if (response.data === true) {
      // 更新本地数据
      const task = tasks.value.find(t => t.id === taskId)
      if (task) {
        task.status = statusKey
      }
      
      ElMessage.success('任务状态更新成功')
    } else {
      ElMessage.error('更新任务状态失败: 权限不足')
      // 恢复原状态
      await fetchTasks()
    }
  } catch (error) {
    ElMessage.error('更新任务状态失败: ' + (error.message || '未知错误'))
    console.error('更新任务状态失败:', error)
    // 恢复原状态
    await fetchTasks()
  }
}

// 检查用户是否有权限更新任务状态
// 检查用户是否有权限更新任务状态
const canUpdateTaskStatus = (task) => {
  const currentUser = userStore.userInfo
  if (!currentUser || !task) return false
  return currentUser.role === 'ADMIN' || 
         (task.assigneeId !== null && task.assigneeId === currentUser.id)
}

// 检查用户是否有权限删除任务
const canDeleteTask = (task) => {
  const currentUser = userStore.userInfo
  const currentProject = project.value
  if (!currentUser || !task || !currentProject) return false
  
  // 1. 管理员可以删除任何任务
  if (currentUser.role === 'ADMIN') return true
  
  // 2. 项目创建者可以删除项目中的任何任务
  if (currentProject.creatorId === currentUser.id) return true
  
  // 3. 任务负责人可以删除自己的任务
  if (task.assigneeId !== null && task.assigneeId === currentUser.id) return true
  
  return false
}

// 检查用户是否可以拖拽任务
const canDragTask = () => {
  return true // 任何人都可以拖拽，但在drop时会检查权限
}

// 删除任务
const handleDeleteTask = (task) => {
  ElMessageBox.confirm(
    `确定要删除任务 "${task.title}" 吗？此操作不可撤销！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const response = await apiDeleteTask(task.id)
      if (response.data === true) {
        ElMessage.success('任务删除成功')
        // 重新获取任务列表
        await fetchTasks()
      } else {
        ElMessage.error('删除任务失败: 权限不足')
      }
    } catch (error) {
      ElMessage.error('删除任务失败: ' + (error.message || '未知错误'))
      console.error('删除任务失败:', error)
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 页面加载时获取数据
onMounted(() => {
  fetchProjectName()
  fetchTasks()
  fetchUsers()
})

// 创建任务
const createTask = async () => {
  if (!taskFormRef.value) return
  
  await taskFormRef.value.validate(async (valid) => {
    if (valid) {
      creatingTask.value = true
      try {
        await apiCreateTask(taskForm)
        ElMessage.success('任务发布成功')
        showCreateTaskDialog.value = false
        // 重置表单
        taskForm.title = ''
        taskForm.description = ''
        taskForm.assigneeId = null
        // 重新获取任务列表
        await fetchTasks()
      } catch (error) {
        ElMessage.error('任务发布失败: ' + (error.message || '未知错误'))
      } finally {
        creatingTask.value = false
      }
    }
  })
}

</script>

<style scoped>
.project-task-view {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-button {
  border: none;
  background: #f5f7fa;
}

.back-button:hover {
  background: #e4e7ed;
  color: #667eea;
}

.header-title h2 {
  margin: 0 0 4px 0;
  font-size: 22px;
  font-weight: 600;
  color: #2c3e50;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.task-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.view-toggle {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.view-toggle :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
}

.board-view {
  margin-top: 24px;
}

.kanban-container {
  display: flex;
  gap: 20px;
  min-height: 600px;
}

.kanban-column {
  flex: 1;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  min-height: 500px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.kanban-column:hover {
  border-color: #e4e7ed;
}

.column-todo {
  border-left: 4px solid #409eff;
}

.column-doing {
  border-left: 4px solid #e6a23c;
}

.column-done {
  border-left: 4px solid #67c23a;
}

.column-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e4e7ed;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.column-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.task-badge {
  margin-left: 8px;
}

.task-list {
  min-height: 100px;
}

.task-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: move;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.task-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.task-card h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.task-card p {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.list-view {
  margin-top: 24px;
}

.list-view :deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

.list-view :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.list-view :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>