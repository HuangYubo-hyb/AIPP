<template>
  <div class="project-list-view">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <el-icon :size="28" class="title-icon"><FolderOpened /></el-icon>
          <h1>项目列表</h1>
        </div>
        <div class="header-actions">

          <el-button 
            v-if="userStore.isAdmin()" 
            type="primary" 
            @click="showCreateDialog = true"
            :icon="Plus"
          >
            创建项目
          </el-button>
          <el-button 
            v-if="userStore.isAdmin()" 
            @click="goToUserManagement"
            :icon="User"
          >
            用户管理
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
    
    <el-card class="project-list-card" shadow="never">
      
      <!-- 过滤和搜索区域 -->
      <div class="filter-section">
        <el-select 
          v-model="selectedType" 
          placeholder="请选择项目类型" 
          clearable 
          @change="applyFilters"
          class="filter-select"
        >
          <el-option label="架构类" value="ARCHITECTURE"></el-option>
          <el-option label="产品类" value="PRODUCT"></el-option>
        </el-select>
        
        <el-input
          v-model="searchKeyword"
          placeholder="请输入项目名称搜索"
          clearable
          @clear="applyFilters"
          class="search-input"
          @keyup.enter="applyFilters"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <!-- 项目表格 -->
      <el-table 
        :data="filteredProjects" 
        style="width: 100%" 
        stripe
        v-loading="loading"
        :row-class-name="tableRowClassName"
        @row-click="goToTasks"
        class="project-table"
      >
        <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="name" label="项目名称" min-width="200">
          <template #default="scope">
            <div class="project-name-cell">
              <el-icon class="project-icon"><Folder /></el-icon>
              <span class="name-text">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip></el-table-column>
        <el-table-column prop="type" label="类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'ARCHITECTURE' ? 'primary' : 'success'" effect="dark">
              {{ scope.row.type === 'ARCHITECTURE' ? '架构类' : '产品类' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="200" align="center"></el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              @click.stop="goToTasks(scope.row)"
              :icon="ArrowRight"
            >
              进入项目
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="filteredProjects.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无项目数据" />
      </div>
    </el-card>
    
    <!-- 创建项目对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建项目" width="500px">
      <el-form 
        ref="projectFormRef" 
        :model="projectForm" 
        :rules="projectFormRules"
        label-width="80px"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="projectForm.name" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="projectForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="项目类型" prop="type">
          <el-select v-model="projectForm.type" style="width: 100%">
            <el-option label="架构类" value="ARCHITECTURE"></el-option>
            <el-option label="产品类" value="PRODUCT"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleCreateProject"
            :loading="creatingProject"
          >
            确认创建
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { getProjects, createProject as apiCreateProject } from '@/api/projectApi'
import { ElCard, ElTable, ElSelect, ElOption, ElInput, ElButton, ElTag, ElMessage, ElDialog, ElForm, ElFormItem, ElEmpty, ElIcon } from 'element-plus'
import { FolderOpened, Plus, User, SwitchButton, Search, Folder, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'

// 路由实例
const router = useRouter()

// 用户状态
const userStore = useUserStore()

// 响应式数据
const projects = ref([])
const selectedType = ref('')
const searchKeyword = ref('')
const loading = ref(false)

// 创建项目相关
const showCreateDialog = ref(false)
const projectFormRef = ref()
const creatingProject = ref(false)

const projectForm = reactive({
  name: '',
  description: '',
  type: ''
})

const projectFormRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ]
}

// 计算属性：根据类型和关键字过滤项目
const filteredProjects = computed(() => {
  let result = projects.value
  
  // 按类型过滤
  if (selectedType.value) {
    result = result.filter(project => project.type === selectedType.value)
  }
  
  // 按关键字搜索（项目名称）
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(project => 
      project.name && project.name.toLowerCase().includes(keyword)
    )
  }
  
  return result
})

// 获取项目列表
const fetchProjects = async () => {
  loading.value = true
  try {
    const response = await getProjects()
    projects.value = response.data
  } catch (error) {
    ElMessage.error('获取项目列表失败: ' + error.message)
    console.error('获取项目列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 应用过滤器（在已有数据上进行过滤）
const applyFilters = () => {
  // 过滤逻辑已在 computed 属性中实现
  // 这里可以添加其他需要执行的操作
}

// 表格行样式
const tableRowClassName = ({ rowIndex }) => {
  if (rowIndex % 2 === 1) {
    return 'even-row'
  }
  return ''
}

// 页面加载时获取数据
onMounted(() => {
  fetchProjects()
})

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}



// 跳转到任务页面
const goToTasks = (project) => {
  router.push(`/projects/${project.id}/tasks`)
}

// 跳转到用户管理页面
const goToUserManagement = () => {
  router.push('/user-management')
}

// 创建项目
const handleCreateProject = async () => {
  if (!projectFormRef.value) return
  
  await projectFormRef.value.validate(async (valid) => {
    if (valid) {
      creatingProject.value = true
      try {
        await apiCreateProject(projectForm)
        ElMessage.success('项目创建成功')
        showCreateDialog.value = false
        // 重置表单
        projectForm.name = ''
        projectForm.description = ''
        projectForm.type = ''
        // 重新获取项目列表
        await fetchProjects()
      } catch (error) {
        ElMessage.error('项目创建失败: ' + (error.message || '未知错误'))
      } finally {
        creatingProject.value = false
      }
    }
  })
}

</script>

<style scoped>
.project-list-view {
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

.project-list-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.filter-section {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-select {
  width: 200px;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.project-table {
  margin-top: 0;
}

.project-table :deep(.el-table__row) {
  cursor: pointer;
  transition: all 0.3s ease;
}

.project-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
  transform: translateX(4px);
}

.project-table :deep(.even-row) {
  background-color: #fafbfc;
}

.project-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.project-icon {
  color: #667eea;
  font-size: 18px;
}

.name-text {
  font-weight: 500;
  color: #2c3e50;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

/* 对话框样式优化 */
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