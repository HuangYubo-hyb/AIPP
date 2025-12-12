package com.aiot.demo1.service;

import com.aiot.demo1.entity.Task;
import com.baomidou.mybatisplus.extension.service.IService;

import java.security.Principal;
import java.util.List;

public interface TaskService extends IService<Task> {
    
    /**
     * 根据项目ID获取任务列表
     * @param projectId 项目ID
     * @return 任务列表
     */
    List<Task> getTasksByProjectId(Long projectId);
    
    /**
     * 根据项目ID获取任务列表，包含负责人姓名
     * @param projectId 项目ID
     * @return 任务列表
     */
    List<Task> getTasksByProjectIdWithAssigneeNames(Long projectId);
    
    /**
     * 保存任务
     * @param task 任务对象
     * @param principal 当前用户信息
     * @return 保存后的任务
     */
    Task saveTask(Task task, Principal principal);
    
    /**
     * 删除任务
     * @param taskId 任务ID
     * @param principal 当前用户信息
     * @return 是否删除成功
     */
    boolean deleteTask(Long taskId, Principal principal);
    
    /**
     * 更新任务状态
     * @param taskId 任务ID
     * @param status 新状态
     * @param principal 当前用户信息
     * @return 是否更新成功
     */
    boolean updateTaskStatus(Long taskId, String status, Principal principal);
    
    /**
     * 更新任务状态（无需用户身份验证的版本）
     * @param taskId 任务ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateTaskStatusWithoutPrincipal(Long taskId, String status);
    
    /**
     * 获取AI任务拆解建议
     * @param description 任务描述
     * @return 任务建议列表
     */
    String getAiTaskSuggestions(String description);
}