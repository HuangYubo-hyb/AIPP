package com.aiot.demo1.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    EMPLOYEE("EMPLOYEE", "普通员工"),
    ADMIN("ADMIN", "管理员");
    
    private final String code;
    private final String description;
    
    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }
}