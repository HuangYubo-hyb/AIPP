import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useProjectStore = defineStore('project', () => {
  const projects = ref([])
  const currentProject = ref(null)

  const setProjects = (data) => {
    projects.value = data
  }

  const setCurrentProject = (project) => {
    currentProject.value = project
  }

  const getProjectById = (id) => {
    return projects.value.find(project => project.id === parseInt(id))
  }

  return {
    projects,
    currentProject,
    setProjects,
    setCurrentProject,
    getProjectById
  }
})