<template>
  <div class="projects-view">
    <h1>项目列表</h1>
    <el-form :inline="true" :model="filterForm" class="filter-form">
      <el-form-item label="项目类型">
        <el-select v-model="filterForm.type" placeholder="请选择项目类型" clearable @change="fetchProjects">
          <el-option label="架构类" value="ARCHITECTURE"></el-option>
          <el-option label="产品类" value="PRODUCT"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchProjects">查询</el-button>
      </el-form-item>
    </el-form>
    
    <el-table :data="projects" style="width: 100%" @row-click="goToTasks">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="项目名称"></el-table-column>
      <el-table-column prop="description" label="描述"></el-table-column>
      <el-table-column prop="type" label="类型" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.type === 'ARCHITECTURE' ? 'primary' : 'success'">
            {{ scope.row.type === 'ARCHITECTURE' ? '架构类' : '产品类' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="200"></el-table-column>
    </el-table>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProjects } from '@/api/projectApi'
import { useProjectStore } from '@/stores/projectStore'

export default {
  name: 'ProjectsView',
  setup() {
    const router = useRouter()
    const projectStore = useProjectStore()
    const projects = ref([])
    const filterForm = ref({
      type: ''
    })

    // 获取项目列表
    const fetchProjects = async () => {
      try {
        const response = await getProjects(filterForm.value)
        projects.value = response.data
        projectStore.setProjects(response.data)
      } catch (error) {
        ElMessage.error('获取项目列表失败')
        console.error(error)
      }
    }

    // 跳转到任务页面
    const goToTasks = (row) => {
      projectStore.setCurrentProject(row)
      router.push(`/projects/${row.id}/tasks`)
    }

    onMounted(() => {
      fetchProjects()
    })

    return {
      projects,
      filterForm,
      fetchProjects,
      goToTasks
    }
  }
}
</script>

<style scoped>
.projects-view {
  padding: 20px;
}

.filter-form {
  margin-bottom: 20px;
}

.el-table {
  cursor: pointer;
}
</style>