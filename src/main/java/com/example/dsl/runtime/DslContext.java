package com.example.dsl.runtime;

import lombok.Getter;

import java.util.*;

/**
 * DSL执行上下文，用于存储变量和函数执行状态
 */
public class DslContext {
    // 作用域栈
    private final Deque<Map<String, Object>> scopeStack = new ArrayDeque<>();
    
    // 全局函数
    private final Map<String, DslFunction> globalFunctions = new HashMap<>();

    // 父上下文，用于支持作用域链
    private final DslContext parent;

    public DslContext() {
        this(null);
    }

    public DslContext(DslContext parent) {
        this.parent = parent;
        // 初始化全局作用域
        scopeStack.push(new HashMap<>());
        initializeGlobalFunctions();
    }

    // 初始化全局函数
    private void initializeGlobalFunctions() {
        // 打印函数
        globalFunctions.put("print", args -> {
            if (args != null && args.length > 0) {
                System.out.println(args[0]);
            }
            return null;
        });

        // 求和函数
        globalFunctions.put("sum", args -> {
            if (args == null || args.length == 0) return 0.0;
            double sum = 0.0;
            for (Object arg : args) {
                if (arg instanceof Number) {
                    sum += ((Number) arg).doubleValue();
                }
            }
            return sum;
        });

        // 数组长度函数
        globalFunctions.put("length", args -> {
            if (args == null || args.length == 0) return 0;
            Object arg = args[0];
            if (arg instanceof List) {
                return ((List<?>) arg).size();
            }
            if (arg instanceof String) {
                return ((String) arg).length();
            }
            return 0;
        });
    }

    // 进入新作用域
    public void enterScope() {
        scopeStack.push(new HashMap<>());
    }

    // 退出当前作用域
    public void exitScope() {
        if (scopeStack.size() > 1) { // 保留全局作用域
            scopeStack.pop();
        }
    }

    // 设置变量
    public void setVariable(String name, Object value) {
        // 首先在当前作用域查找变量
        for (Map<String, Object> scope : scopeStack) {
            if (scope.containsKey(name)) {
                scope.put(name, value);
                return;
            }
        }
        // 如果没找到，则在当前作用域创建新变量
        scopeStack.peek().put(name, value);
    }

    // 获取变量
    public Object getVariable(String name) {
        // 在作用域栈中查找变量
        for (Map<String, Object> scope : scopeStack) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }

        // 如果当前上下文没有找到，则查找父上下文
        if (parent != null) {
            return parent.getVariable(name);
        }

        return null;
    }

    // 获取全局函数
    public DslFunction getGlobalFunction(String name) {
        return globalFunctions.get(name);
    }

    // 创建子上下文
    public DslContext createChildContext() {
        return new DslContext(this);
    }

    // 判断变量是否在当前作用域中已定义
    public boolean isVariableDefined(String name) {
        return scopeStack.peek().containsKey(name);
    }
}
