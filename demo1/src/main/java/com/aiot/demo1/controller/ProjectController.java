package com.aiot.demo1.controller;

import com.aiot.demo1.entity.Project;
import com.aiot.demo1.entity.Task;
import com.aiot.demo1.service.ProjectService;
import com.aiot.demo1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取项目列表，支持通过type查询参数过滤
     * @param type 项目类型
     * @return 项目列表
     */
    @GetMapping
    public List<Project> getProjects(@RequestParam(required = false) String type) {
        if (type != null && !type.isEmpty()) {
            return projectService.getProjectsByType(type);
        }
        return projectService.list();
    }
    
    /**
     * 获取指定项目详情
     * @param id 项目ID
     * @return 项目详情
     */
    @GetMapping("/{id}")
    public Project getProjectById(@PathVariable Long id) {
        return projectService.getById(id);
    }
    
    /**
     * 创建项目（仅管理员）
     * @param project 项目信息
     * @param principal 当前用户信息
     * @return 创建的项目
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Project createProject(@RequestBody Project project, Principal principal) {
        // 设置创建者为当前用户
        project.setCreatorId(userService.findByUsername(principal.getName()).getId());
        return projectService.saveProject(project);
    }
    
    /**
     * 获取指定项目下的任务列表
     * @param id 项目ID
     * @return 任务列表
     */
    @GetMapping("/{id}/tasks")
    public List<Task> getTasksByProjectId(@PathVariable Long id) {
        return projectService.getTasksByProjectId(id);
    }
}