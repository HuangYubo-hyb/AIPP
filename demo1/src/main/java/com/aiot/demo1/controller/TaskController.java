package com.aiot.demo1.controller;

import com.aiot.demo1.entity.Task;
import com.aiot.demo1.service.TaskService;
import com.aiot.demo1.enums.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    /**
     * 根据项目ID获取任务列表
     * @param projectId 项目ID
     * @return 任务列表
     */
    @GetMapping("/project/{projectId}")
    public List<Task> getTasksByProjectId(@PathVariable Long projectId) {
        return taskService.getTasksByProjectIdWithAssigneeNames(projectId);
    }
    
    /**
     * 创建/发布任务（管理员和普通用户都可以创建任务）
     * @param task 任务信息
     * @param principal 当前用户信息
     * @return 创建的任务
     */
    @PostMapping
    public Task createTask(@RequestBody Task task, Principal principal) {
        // 设置默认状态为TODO
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());
        taskService.saveTask(task, principal);  // 传递principal参数用于权限控制
        return task;
    }
    
    /**
     * 更新任务状态（用于看板拖拽）
     * @param id 任务ID
     * @param status 新状态
     * @param principal 当前用户信息
     * @return 是否更新成功
     */
    @PutMapping("/{id}/status")
    public boolean updateTaskStatus(@PathVariable Long id, @RequestParam String status, Principal principal) {
        return taskService.updateTaskStatus(id, status, principal);
    }
    
    /**
     * 删除任务
     * @param id 任务ID
     * @param principal 当前用户信息
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public boolean deleteTask(@PathVariable Long id, Principal principal) {
        return taskService.deleteTask(id, principal);
    }
    
    /**
     * 为AI功能预留的端点，接收文本描述，返回任务拆解建议（初期可返回模拟数据）
     * @param description 任务描述
     * @return 任务建议
     */
    @PostMapping("/ai-suggestions")
    public String getAiTaskSuggestions(@RequestBody String description) {
        return taskService.getAiTaskSuggestions(description);
    }
}