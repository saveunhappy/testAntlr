package com.example.dsl.runtime;

import com.example.dsl.BusinessDslParser;
import com.example.model.DslFunction;
import com.example.model.DslScript;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DslEngine {

    private final Map<String, DslScript> loadedScripts = new HashMap<>();

    // 加载脚本
    public void loadScript(DslScript script) {
        loadedScripts.put(script.getId(), script);
        log.info("加载脚本: {}", script.getName());
    }

    // 卸载脚本
    public void unloadScript(String scriptId) {
        if (loadedScripts.containsKey(scriptId)) {
            DslScript script = loadedScripts.remove(scriptId);
            log.info("卸载脚本: {}", script.getName());
        }
    }

    // 执行脚本中的函数
    public Object executeFunction(String scriptId, String functionName, Object... args) {
        DslScript script = loadedScripts.get(scriptId);
        if (script == null) {
            throw new RuntimeException("脚本未加载: " + scriptId);
        }

        DslFunction function = script.getFunction(functionName);
        if (function == null) {
            throw new RuntimeException("函数未找到: " + functionName);
        }

        return executeFunction(script, function, args);
    }

    // 执行函数
    private Object executeFunction(DslScript script, DslFunction function, Object... args) {
        // 创建执行上下文
        DslContext context = new DslContext();

        // 绑定参数
        for (int i = 0; i < Math.min(function.getParameters().size(), args.length); i++) {
            context.setVariable(function.getParameters().get(i), args[i]);
        }

        try {
            // 执行函数体
            return executeBlock((BusinessDslParser.BlockContext) function.getBody(), context);
        } catch (Exception e) {
            log.error("执行函数失败: {}.{} - {}", script.getName(), function.getName(), e.getMessage(), e);
            throw new RuntimeException("执行函数失败: " + e.getMessage(), e);
        }
    }

    // 执行代码块
    private Object executeBlock(BusinessDslParser.BlockContext blockContext, DslContext context) {
        // 这里应该实现完整的解释器逻辑
        // 为了简化示例，我们只实现一个基本框架

        // 创建块级作用域
        DslContext dslContext = context.createChildContext();

        // 遍历并执行块中的每个语句
        Object result = null;
        for (BusinessDslParser.StatementContext stmt : blockContext.statement()) {
            // 根据语句类型执行不同的操作
            // 这里需要实现完整的解释器逻辑

            // 示例：处理返回语句
            if (stmt.returnStatement() != null) {
                if (stmt.returnStatement().expr() != null) {
                    // 计算返回表达式的值
                    result = evaluateExpression(stmt.returnStatement().expr(), dslContext);
                }
                break; // 遇到return语句就退出循环
            }

            // 处理其他类型的语句...
        }

        return result;
    }

    // 计算表达式的值
    private Object evaluateExpression(BusinessDslParser.ExprContext exprContext, DslContext context) {
        // 这里应该实现完整的表达式求值逻辑
        // 为了简化示例，我们只返回一个占位符
        return "表达式结果";
    }

    // 获取已加载的脚本
    public Map<String, DslScript> getLoadedScripts() {
        return new HashMap<>(loadedScripts);
    }
}
