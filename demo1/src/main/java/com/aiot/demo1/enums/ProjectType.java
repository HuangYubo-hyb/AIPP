package com.aiot.demo1.enums;

import lombok.Getter;

@Getter
public enum ProjectType {
    ARCHITECTURE("ARCHITECTURE", "架构类"),
    PRODUCT("PRODUCT", "产品类");
    
    private final String code;
    private final String description;
    
    ProjectType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}