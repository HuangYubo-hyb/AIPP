package com.aiot.demo1.entity;

import com.aiot.demo1.enums.TaskStatus;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String description;
    
    @EnumValue
    private TaskStatus status;
    
    private Long projectId;
    
    private Long assigneeId;
    
    private Long creatorId;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
    
    @TableField(exist = false)
    private String assignee;
    
    @TableField(exist = false)
    private String creator;
}