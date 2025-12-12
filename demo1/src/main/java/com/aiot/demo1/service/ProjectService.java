package com.aiot.demo1.service;

import com.aiot.demo1.entity.Project;
import com.aiot.demo1.entity.Task;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ProjectService extends IService<Project> {
    
    /**
     * 根据项目类型获取项目列表
     * @param type 项目类型
     * @return 项目列表
     */
    List<Project> getProjectsByType(String type);
    
    /**
     * 根据项目ID获取任务列表
     * @param projectId 项目ID
     * @return 任务列表
     */
    List<Task> getTasksByProjectId(Long projectId);
    
    /**
     * 保存项目
     * @param project 项目对象
     * @return 保存后的项目
     */
    Project saveProject(Project project);
}