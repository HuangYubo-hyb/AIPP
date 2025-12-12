package com.aiot.demo1.service.impl;

import com.aiot.demo1.entity.Project;
import com.aiot.demo1.entity.Task;
import com.aiot.demo1.entity.User;
import com.aiot.demo1.mapper.TaskMapper;
import com.aiot.demo1.service.ProjectService;
import com.aiot.demo1.service.TaskService;
import com.aiot.demo1.service.UserService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {
    
    @Autowired
    private TaskMapper taskMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProjectService projectService;
    

    
    @Override
    public List<Task> getTasksByProjectId(Long projectId) {
        return taskMapper.selectList(
                new QueryWrapper<Task>()
                        .eq("project_id", projectId)
        );
    }
    
    @Override
    public List<Task> getTasksByProjectIdWithAssigneeNames(Long projectId) {
        // 使用关联查询获取任务及负责人名称
        return taskMapper.selectList(
                new QueryWrapper<Task>()
                        .eq("project_id", projectId)
        );
    }
    
    @Override
    public Task saveTask(Task task, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        
        // 设置任务创建者
        task.setCreatorId(currentUser.getId());
        
        // 检查权限：管理员可以给任何人分配任务，普通用户只能给自己或他人分配任务（无特殊限制）
        // 在实际应用中，可以根据业务需求进一步细化权限控制
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());
        this.save(task);
        

        
        return task;
    }
    
    @Override
    public boolean deleteTask(Long taskId, Principal principal) {
        Task task = this.getById(taskId);
        if (task != null) {
            User currentUser = userService.findByUsername(principal.getName());
            
            // 获取任务所属项目
            Project project = projectService.getById(task.getProjectId());
            
            // 检查权限：1.管理员 2.项目创建者 3.任务负责人
            boolean hasPermission = 
                currentUser.getRole().equals(com.aiot.demo1.enums.UserRole.ADMIN) || 
                (project != null && project.getCreatorId().equals(currentUser.getId())) ||
                (task.getAssigneeId() != null && task.getAssigneeId().equals(currentUser.getId()));
            
            if (hasPermission) {
                return this.removeById(taskId);
            }
        }
        return false;
    }
    
    @Override
    public boolean updateTaskStatus(Long taskId, String status, Principal principal) {
        Task task = this.getById(taskId);
        if (task != null) {
            // 检查权限：管理员或任务负责人才能更新状态
            User currentUser = userService.findByUsername(principal.getName());
            
            boolean hasPermission = currentUser.getRole().equals(com.aiot.demo1.enums.UserRole.ADMIN) || 
                (task.getAssigneeId() != null && task.getAssigneeId().equals(currentUser.getId()));
            
            if (hasPermission) {
                task.setStatus(com.aiot.demo1.enums.TaskStatus.valueOf(status));
                task.setUpdatedTime(LocalDateTime.now());
                return this.updateById(task);
            }
        }
        return false;
    }
    
    @Override
    public boolean updateTaskStatusWithoutPrincipal(Long taskId, String status) {
        Task task = this.getById(taskId);
        if (task != null) {
            task.setStatus(com.aiot.demo1.enums.TaskStatus.valueOf(status));
            task.setUpdatedTime(LocalDateTime.now());
            return this.updateById(task);
        }
        return false;
    }
    
    @Override
    public String getAiTaskSuggestions(String description) {
        // 模拟AI任务拆解建议
        return "{\n" +
                "  \"suggestions\": [\n" +
                "    {\n" +
                "      \"title\": \"需求分析\",\n" +
                "      \"description\": \"分析并明确\\\"" + description + "\\\"的具体需求\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"title\": \"技术选型\",\n" +
                "      \"description\": \"确定实现该需求所需的技术栈和框架\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"title\": \"设计阶段\",\n" +
                "      \"description\": \"设计具体的实现方案和架构\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"title\": \"编码实现\",\n" +
                "      \"description\": \"按照设计方案进行编码实现\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"title\": \"测试验证\",\n" +
                "      \"description\": \"对实现的功能进行全面测试\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
    

}