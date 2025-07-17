package com.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
