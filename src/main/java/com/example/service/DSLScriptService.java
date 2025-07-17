package com.example.service;

import com.example.dsl.BusinessDslLexer;
import com.example.dsl.BusinessDslParser;
import com.example.dsl.parser.BusinessDslVisitorImpl;
import com.example.dsl.parser.DslParser;
import com.example.model.DslScript;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DSL脚本服务
 * 负责脚本的加载、保存、验证和执行
 */
@Slf4j
@Service
public class DSLScriptService {
    
    @Value("${dsl.scripts.path:scripts}")
    private String scriptsPath;
    
    private final DslParser dslParser;
    
    public DSLScriptService(DslParser dslParser) {
        this.dslParser = dslParser;
    }
    
    /**
     * 获取所有脚本列表
     */
    public Map<String, String> listScripts() throws IOException {
        Path scriptDir = Paths.get(scriptsPath);
        if (!Files.exists(scriptDir)) {
            Files.createDirectories(scriptDir);
        }
        
        Map<String, String> scripts = new HashMap<>();
        Files.list(scriptDir)
            .filter(path -> path.toString().endsWith(".dsl"))
            .forEach(path -> {
                try {
                    String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                    scripts.put(path.getFileName().toString(), content);
                } catch (IOException e) {
                    log.error("读取脚本失败: {}", path, e);
                }
            });
        return scripts;
    }
    
    /**
     * 获取脚本内容
     */
    public String getScript(String scriptName) throws IOException {
        Path scriptPath = Paths.get(scriptsPath, scriptName);
        if (!Files.exists(scriptPath)) {
            return null;
        }
        return new String(Files.readAllBytes(scriptPath), StandardCharsets.UTF_8);
    }
    
    /**
     * 保存脚本
     */
    public void saveScript(String scriptName, String content) throws IOException {
        Path scriptDir = Paths.get(scriptsPath);
        if (!Files.exists(scriptDir)) {
            Files.createDirectories(scriptDir);
        }
        
        // 确保脚本名称以.dsl结尾
        if (!scriptName.endsWith(".dsl")) {
            scriptName = scriptName + ".dsl";
        }
        
        Path scriptPath = scriptDir.resolve(scriptName);
        Files.write(scriptPath, content.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 删除脚本
     */
    public void deleteScript(String scriptName) throws IOException {
        Path scriptPath = Paths.get(scriptsPath, scriptName);
        if (Files.exists(scriptPath)) {
            Files.delete(scriptPath);
        }
    }
    
    /**
     * 执行脚本
     */
    public Object executeScript(String scriptName, Map<String, Object> context) throws Exception {
        String content = getScript(scriptName);
        if (content == null) {
            throw new IllegalArgumentException("脚本不存在: " + scriptName);
        }

        // 创建访问者
        BusinessDslVisitorImpl visitor = new BusinessDslVisitorImpl(scriptName);
        
        // 设置执行模式标志
        visitor.setVariable("__EXECUTE_MODE__", true);
        
        // 根据脚本名称确定要执行的函数
        if (scriptName.equals("pricing.dsl")) {
            visitor.setVariable("__FUNCTION_TO_EXECUTE__", "calculatePrice");
        } else if (scriptName.equals("discount.dsl")) {
            visitor.setVariable("__FUNCTION_TO_EXECUTE__", "calculateDiscount");
        } else if (scriptName.equals("logic.dsl")) {
            visitor.setVariable("__FUNCTION_TO_EXECUTE__", "logicTest");
        }
        
        // 设置上下文变量
        if (context != null) {
            context.forEach(visitor::setVariable);
        }

        // 创建词法分析器
        BusinessDslLexer lexer = new BusinessDslLexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BusinessDslParser parser = new BusinessDslParser(tokens);
        
        // 获取解析树并执行
        Object result = visitor.visit(parser.program());
        
        // 如果结果不是Map类型，将其包装成Map
        if (!(result instanceof Map)) {
            Map<String, Object> wrappedResult = new HashMap<>();
            wrappedResult.put("result", result);
            return wrappedResult;
        }
        
        return result;
    }

    /**
     * 加载脚本
     */
    public DslScript loadScript(String scriptName) throws Exception {
        String content = getScript(scriptName);
        if (content == null) {
            throw new IllegalArgumentException("脚本不存在: " + scriptName);
        }
        
        // 创建访问者（不设置执行模式，只解析脚本）
        BusinessDslVisitorImpl visitor = new BusinessDslVisitorImpl(scriptName);
        
        // 创建词法分析器
        BusinessDslLexer lexer = new BusinessDslLexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BusinessDslParser parser = new BusinessDslParser(tokens);
        
        // 获取解析树并访问
        Object result = visitor.visit(parser.program());
        
        if (result instanceof DslScript) {
            DslScript script = (DslScript) result;
            script.setContent(content);
            return script;
        }
        throw new RuntimeException("解析脚本失败: 返回类型不是DslScript");
    }
    
    /**
     * 验证脚本语法
     */
    public boolean validateScript(String content) {
        try {
            dslParser.parse("validate.dsl", content);
            return true;
        } catch (Exception e) {
            log.error("脚本语法错误", e);
            return false;
        }
    }
}
