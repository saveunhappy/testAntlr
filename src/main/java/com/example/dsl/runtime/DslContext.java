package com.example.dsl.runtime;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * DSL执行上下文，用于存储变量和函数执行状态
 */
public class DslContext {
    @Getter
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, Object> globalFunctions = new HashMap<>();

    // 父上下文，用于支持作用域链
    private final DslContext parent;

    public DslContext() {
        this(null);
    }

    public DslContext(DslContext parent) {
        this.parent = parent;
//        initializeGlobalFunctions();
    }

    // 初始化全局函数
//    private void initializeGlobalFunctions() {
//        // 添加内置函数
//        globalFunctions.put("print", args -> {
//
//            if (args.length > 0) {
//                System.out.println(args[0]);
//            }
//            return null;
//        });
//
//        globalFunctions.put("sum", args -> {
//            if (args.length == 0) return 0;
//            double sum = 0;
//            for (Object arg : args) {
//                if (arg instanceof Number) {
//                    sum += ((Number) arg).doubleValue();
//                }
//            }
//            return sum;
//        });
//
//        // 可以添加更多内置函数...
//    }

    // 设置变量
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    // 获取变量
    public Object getVariable(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }

        // 如果当前上下文没有找到，则查找父上下文
        if (parent != null) {
            return parent.getVariable(name);
        }

        return null;
    }

    // 获取全局函数
    public Object getGlobalFunction(String name) {
        return globalFunctions.get(name);
    }

    // 创建子上下文
    public DslContext createChildContext() {
        return new DslContext(this);
    }
}
