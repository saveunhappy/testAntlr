package com.example.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DslFunction {
    private String name;
    private List<String> parameters = new ArrayList<>();
    private Object body; // 存储函数体的AST或可执行对象

    public DslFunction(String name) {
        this.name = name;
    }

    public void addParameter(String param) {
        parameters.add(param);
    }
}
