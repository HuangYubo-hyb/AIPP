package com.aiot.demo1.service.impl;

import com.aiot.demo1.entity.Project;
import com.aiot.demo1.entity.Task;
import com.aiot.demo1.mapper.ProjectMapper;
import com.aiot.demo1.mapper.TaskMapper;
import com.aiot.demo1.service.ProjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {
    
    @Autowired
    private TaskMapper taskMapper;
    
    @Override
    public List<Project> getProjectsByType(String type) {
        return this.lambdaQuery()
                .eq(Project::getType, type)
                .list();
    }
    
    @Override
    public List<Task> getTasksByProjectId(Long projectId) {
        return taskMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Task>()
                        .eq("project_id", projectId)
        );
    }
    
    @Override
    public Project saveProject(Project project) {
        project.setCreatedTime(LocalDateTime.now());
        this.save(project);
        return project;
    }
}