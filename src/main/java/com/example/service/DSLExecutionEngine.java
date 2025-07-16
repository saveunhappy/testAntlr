//package com.example.service;
//
//import com.example.dsl.BusinessDSLLexer;
//import com.example.dsl.BusinessDSLParser;
//import com.example.dsl.visitor.DSLVisitor;
//import lombok.extern.slf4j.Slf4j;
//import org.antlr.v4.runtime.CharStreams;
//import org.antlr.v4.runtime.CommonTokenStream;
//import org.antlr.v4.runtime.tree.ParseTree;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * DSL执行引擎
// * 平台方提供的基础设施，负责解析和执行业务方的DSL脚本
// */
//@Slf4j
//@Component
//public class DSLExecutionEngine {
//
//    private final DSLVisitor visitor;
//    private final Map<String, Object> globalVariables;
//
//    public DSLExecutionEngine() {
//        this.visitor = new DSLVisitor();
//        this.globalVariables = new ConcurrentHashMap<>();
//
//        // 注册内置函数
//        registerBuiltinFunctions();
//    }
//
//    /**
//     * 执行DSL脚本
//     */
//    public Object executeScript(String scriptName, String scriptContent, Map<String, Object> context) {
//        try {
//            log.info("开始执行DSL脚本: {}", scriptName);
//
//            // 解析DSL脚本
//            BusinessDSLLexer lexer = new BusinessDSLLexer(CharStreams.fromString(scriptContent));
//            CommonTokenStream tokens = new CommonTokenStream(lexer);
//            BusinessDSLParser parser = new BusinessDSLParser(tokens);
//            ParseTree tree = parser.program();
//
//            // 设置执行上下文
//            visitor.setContext(context);
//            visitor.setGlobalVariables(globalVariables);
//
//            // 执行脚本
//            Object result = visitor.visit(tree);
//
//            log.info("DSL脚本执行完成: {}, 结果: {}", scriptName, result);
//            return result;
//
//        } catch (Exception e) {
//            log.error("DSL脚本执行失败: {}", scriptName, e);
//            throw new DSLExecutionException("脚本执行失败: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 注册内置函数
//     */
//    private void registerBuiltinFunctions() {
//        // 数学函数
//        visitor.registerFunction("abs", args -> Math.abs(((Number) args[0]).doubleValue()));
//        visitor.registerFunction("max", args -> Math.max(((Number) args[0]).doubleValue(), ((Number) args[1]).doubleValue()));
//        visitor.registerFunction("min", args -> Math.min(((Number) args[0]).doubleValue(), ((Number) args[1]).doubleValue()));
//        visitor.registerFunction("round", args -> Math.round(((Number) args[0]).doubleValue()));
//
//        // 字符串函数
//        visitor.registerFunction("length", args -> ((String) args[0]).length());
//        visitor.registerFunction("substring", args -> {
//            String str = (String) args[0];
//            int start = ((Number) args[1]).intValue();
//            int end = args.length > 2 ? ((Number) args[2]).intValue() : str.length();
//            return str.substring(start, end);
//        });
//        visitor.registerFunction("toUpperCase", args -> ((String) args[0]).toUpperCase());
//        visitor.registerFunction("toLowerCase", args -> ((String) args[0]).toLowerCase());
//
//        // 业务函数
//        visitor.registerFunction("calculateDiscount", args -> {
//            double amount = ((Number) args[0]).doubleValue();
//            double rate = ((Number) args[1]).doubleValue();
//            return amount * rate;
//        });
//
//        visitor.registerFunction("validateOrder", args -> {
//            double amount = ((Number) args[0]).doubleValue();
//            return amount > 0 && amount < 10000;
//        });
//
//        visitor.registerFunction("getCustomerLevel", args -> {
//            double totalSpent = ((Number) args[0]).doubleValue();
//            if (totalSpent >= 10000) return "VIP";
//            if (totalSpent >= 5000) return "GOLD";
//            if (totalSpent >= 1000) return "SILVER";
//            return "BRONZE";
//        });
//
//        // 日志函数
//        visitor.registerFunction("log", args -> {
//            log.info("DSL日志: {}", args[0]);
//            return null;
//        });
//
//        visitor.registerFunction("logError", args -> {
//            log.error("DSL错误: {}", args[0]);
//            return null;
//        });
//    }
//
//    /**
//     * 获取全局变量
//     */
//    public Map<String, Object> getGlobalVariables() {
//        return new ConcurrentHashMap<>(globalVariables);
//    }
//
//    /**
//     * 设置全局变量
//     */
//    public void setGlobalVariable(String name, Object value) {
//        globalVariables.put(name, value);
//    }
//
//    /**
//     * 清除全局变量
//     */
//    public void clearGlobalVariables() {
//        globalVariables.clear();
//    }
//}
