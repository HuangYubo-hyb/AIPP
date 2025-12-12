package com.aiot.demo1.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {
    TODO("TODO", "待办"),
    DOING("DOING", "进行中"),
    DONE("DONE", "已完成");
    
    private final String code;
    private final String description;
    
    TaskStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}