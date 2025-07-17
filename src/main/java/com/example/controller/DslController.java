package com.example.controller;

import com.example.service.DSLScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * DSL REST API控制器
 * 提供脚本管理和执行的HTTP接口
 */
@Slf4j
@RestController
@RequestMapping("/api/dsl")
public class DslController {
    
    @Autowired
    private DSLScriptService scriptService;
    
    /**
     * 获取所有脚本列表
     */
    @GetMapping("/scripts")
    public ResponseEntity<Map<String, Object>> listScripts() {
        try {
            Map<String, String> scripts = scriptService.listScripts();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", scripts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取脚本列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取脚本内容
     */
    @GetMapping("/scripts/{scriptName}")
    public ResponseEntity<Map<String, Object>> getScript(@PathVariable String scriptName) {
        try {
            String content = scriptService.getScript(scriptName);
            Map<String, Object> response = new HashMap<>();
            if (content != null) {
                response.put("success", true);
                response.put("data", content);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "脚本不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取脚本失败: {}", scriptName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 保存脚本
     */
    @PostMapping("/scripts/{scriptName}")
    public ResponseEntity<Map<String, Object>> saveScript(
            @PathVariable String scriptName,
            @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            if (content == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "脚本内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            scriptService.saveScript(scriptName, content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "脚本保存成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("保存脚本失败: {}", scriptName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 删除脚本
     */
    @DeleteMapping("/scripts/{scriptName}")
    public ResponseEntity<Map<String, Object>> deleteScript(@PathVariable String scriptName) {
        try {
            scriptService.deleteScript(scriptName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "脚本删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除脚本失败: {}", scriptName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 执行脚本
     */
    @PostMapping("/scripts/{scriptName}/execute")
    public ResponseEntity<Map<String, Object>> executeScript(
            @PathVariable String scriptName,
            @RequestBody(required = false) Map<String, Object> context) {
        try {
            if (context == null) {
                context = new HashMap<>();
                // 添加默认测试参数
                if (scriptName.equals("discount.dsl")) {
                    context.put("productId", "PROD001");
                    context.put("price", 100.0);
                    context.put("userId", "USER001");
                } else if (scriptName.equals("pricing.dsl")) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", "PROD001");
                    product.put("basePrice", 1500.0);
                    product.put("category", "electronics");
                    product.put("stock", 50);
                    product.put("season", "summer");
                    context.put("product", product);
                }
            }
            
            log.info("执行脚本: {}, 上下文: {}", scriptName, context);
            Object result = scriptService.executeScript(scriptName, context);
            log.info("执行结果: {}", result);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("执行脚本失败: {}", scriptName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "执行失败: " + e.getMessage());
            
            // 收集堆栈信息
            List<String> stackTrace = new ArrayList<>();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace.add(element.toString());
            }
            response.put("stackTrace", stackTrace);
            
            // 如果有原因异常，也包含进来
            if (e.getCause() != null) {
                response.put("cause", e.getCause().getMessage());
            }
            
            return ResponseEntity.ok().body(response); // 改用 ok() 而不是 internalServerError()
        }
    }
    
    /**
     * 验证脚本语法
     */
    @PostMapping("/scripts/validate")
    public ResponseEntity<Map<String, Object>> validateScript(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            if (content == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "脚本内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean isValid = scriptService.validateScript(content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valid", isValid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("验证脚本失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取系统信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Business DSL Engine");
        info.put("version", "1.0.0");
        info.put("description", "基于ANTLR4的业务DSL执行引擎");
        info.put("features", new String[]{
            "脚本热加载",
            "语法高亮",
            "实时调试",
            "内置函数库"
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", info);
        return ResponseEntity.ok(response);
    }
}
