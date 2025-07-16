package com.example.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class DslScript {
    private String id;
    private String name;
    private String content;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean enabled;
    private Map<String, DslFunction> functions = new HashMap<>();

    // 添加函数
    public void addFunction(DslFunction function) {
        functions.put(function.getName(), function);
    }

    // 获取函数
    public DslFunction getFunction(String name) {
        return functions.get(name);
    }
}
