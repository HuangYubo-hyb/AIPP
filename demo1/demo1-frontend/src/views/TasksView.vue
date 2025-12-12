<template>
  <div class="tasks-view">
    <el-page-header @back="goBack">
      <template #content>
        <span>{{ projectName }}</span>
      </template>
      <template #extra>
        <el-button 
          type="primary" 
          @click="handleLogout"
        >
          退出登录
        </el-button>
      </template>
    </el-page-header>
    
    <div class="kanban-container">
      <div v-for="status in taskStatuses" :key="status.key" class="kanban-column" :data-status="status.key">
        <div class="column-header">
          <h3>{{ status.label }}</h3>
          <span class="task-count">({{ getTasksByStatus(status.key).length }})</span>
        </div>
        <draggable 
          :list="getTasksByStatus(status.key)" 
          group="tasks" 
          item-key="id"
          @end="onTaskDrop"
          class="task-list"
        >
          <template #item="{ element }">
            <div class="task-card" @click="openTaskDetail(element)" :data-id="element.id">
              <h4>{{ element.title }}</h4>
              <p>{{ element.description }}</p>
              <div class="task-footer">
                <el-tag size="small" v-if="element.assignee">{{ element.assignee }}</el-tag>
                <span class="update-time">{{ formatDate(element.updatedTime) }}</span>
              </div>
            </div>
          </template>
        </draggable>
      </div>
    </div>
    
    <!-- 任务详情对话框 -->
    <el-dialog v-model="dialogVisible" :title="currentTask.title" width="50%">
      <el-form :model="currentTask" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="currentTask.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="currentTask.description" type="textarea" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="currentTask.status" placeholder="请选择状态">
            <el-option 
              v-for="status in taskStatuses" 
              :key="status.key" 
              :label="status.label" 
              :value="status.key"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateTask">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import draggable from 'vuedraggable'
import { getTasksByProjectId, updateTaskStatus } from '@/api/taskApi'
import { useProjectStore } from '@/stores/projectStore'
import { useTaskStore } from '@/stores/taskStore'
import { useUserStore } from '@/stores/userStore'

export default {
  name: 'TasksView',
  components: {
    draggable
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const projectStore = useProjectStore()
    const taskStore = useTaskStore()
    const userStore = useUserStore()
    
    const projectName = computed(() => projectStore.currentProject?.name || '项目任务')
    const tasks = ref([])
    const dialogVisible = ref(false)
    const currentTask = ref({})
    
    const taskStatuses = [
      { key: 'TODO', label: '待办' },
      { key: 'DOING', label: '进行中' },
      { key: 'DONE', label: '已完成' }
    ]
    
    // 根据状态获取任务列表
    const getTasksByStatus = (status) => {
      return tasks.value.filter(task => task.status === status)
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleDateString()
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
    
    // 打开任务详情
    const openTaskDetail = (task) => {
      currentTask.value = { ...task }
      dialogVisible.value = true
    }
    
    // 更新任务
    const updateTask = async () => {
      try {
        await updateTaskStatus(currentTask.value.id, currentTask.value.status)
        
        // 更新本地任务状态
        const index = tasks.value.findIndex(t => t.id === currentTask.value.id)
        if (index !== -1) {
          tasks.value[index] = { ...currentTask.value }
          taskStore.updateTaskStatus(currentTask.value.id, currentTask.value.status)
        }
        
        ElMessage.success('任务更新成功')
        dialogVisible.value = false
      } catch (error) {
        ElMessage.error('任务更新失败')
        console.error(error)
      }
    }
    
    // 任务拖拽结束事件
    const onTaskDrop = async (event) => {
      try {
        const { item, to } = event
        const taskId = parseInt(item.dataset.id)
        const newStatus = to.parentElement.dataset.status
        
        // 更新任务状态
        await updateTaskStatus(taskId, newStatus)
        
        const task = tasks.value.find(t => t.id === taskId)
        if (task) {
          task.status = newStatus
          taskStore.updateTaskStatus(taskId, newStatus)
          ElMessage.success('任务状态更新成功')
        }
      } catch (error) {
        ElMessage.error('任务状态更新失败: ' + (error.message || '未知错误'))
        console.error(error)
      }
    }
    
    // 获取任务列表
    const fetchTasks = async () => {
      try {
        const response = await getTasksByProjectId(route.params.id)
        tasks.value = response.data
        taskStore.setTasks(response.data)
      } catch (error) {
        ElMessage.error('获取任务列表失败')
        console.error(error)
      }
    }
    
    onMounted(() => {
      fetchTasks()
    })
    
    return {
      projectName,
      tasks,
      dialogVisible,
      currentTask,
      taskStatuses,
      getTasksByStatus,
      formatDate,
      goBack,
      handleLogout,
      openTaskDetail,
      updateTask,
      onTaskDrop,
      fetchTasks
    }
  }
}
</script>

<style scoped>
.tasks-view {
  padding: 20px;
}

.kanban-container {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.kanban-column {
  flex: 1;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 10px;
}

.column-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.column-header h3 {
  margin: 0;
  flex: 1;
}

.task-list {
  min-height: 100px;
}

.task-card {
  background: white;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.task-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.task-card h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #303133;
}

.task-card p {
  margin: 0 0 10px 0;
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.update-time {
  font-size: 12px;
  color: #909399;
}
</style>