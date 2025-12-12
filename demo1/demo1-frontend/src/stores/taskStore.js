import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTaskStore = defineStore('task', () => {
  const tasks = ref([])

  const setTasks = (data) => {
    tasks.value = data
  }

  const updateTaskStatus = (taskId, status) => {
    const task = tasks.value.find(t => t.id === taskId)
    if (task) {
      task.status = status
    }
  }

  const getTasksByProjectId = (projectId) => {
    return tasks.value.filter(task => task.projectId === parseInt(projectId))
  }

  const getTasksByStatus = (status) => {
    return tasks.value.filter(task => task.status === status)
  }

  return {
    tasks,
    setTasks,
    updateTaskStatus,
    getTasksByProjectId,
    getTasksByStatus
  }
})