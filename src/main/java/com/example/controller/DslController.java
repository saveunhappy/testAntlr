package com.example.controller;

import com.example.dsl.parser.DslParser;
import com.example.dsl.runtime.DslEngine;
import com.example.dsl.runtime.DslScriptLoader;
import com.example.model.DslScript;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dsl")
@RequiredArgsConstructor
public class DslController {

    private final DslParser dslParser;
    private final DslEngine dslEngine;
    private final DslScriptLoader dslScriptLoader;

    /**
     * 获取所有已加载的脚本
     */
    @GetMapping("/scripts")
    public ResponseEntity<Map<String, DslScript>> getScripts() {
        return ResponseEntity.ok(dslEngine.getLoadedScripts());
    }

    /**
     * 获取脚本内容
     */
    @GetMapping("/scripts/{scriptId}")
    public ResponseEntity<DslScript> getScript(@PathVariable String scriptId) {
        DslScript script = dslEngine.getLoadedScripts().get(scriptId);
        if (script == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(script);
    }

    /**
     * 保存脚本
     */
    @PostMapping("/scripts/{scriptId}")
    public ResponseEntity<?> saveScript(@PathVariable String scriptId, @RequestBody String content) {
        try {
            // 先解析脚本，确保语法正确
            dslParser.parse(scriptId, content);

            // 保存脚本
            dslScriptLoader.saveScript(scriptId, content);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("保存脚本失败: {}", e.getMessage(), e);
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            return ResponseEntity.badRequest().body(new HashMap<>().put("message", e.getMessage()));
        }
    }

    /**
     * 执行脚本函数
     */
    @PostMapping("/execute/{scriptId}/{functionName}")
    public ResponseEntity<?> executeFunction(
            @PathVariable String scriptId,
            @PathVariable String functionName,
            @RequestBody(required = false) Map<String, Object> params) {

        if (params == null) {
            params = new HashMap<>();
        }

        try {
            Object result = dslEngine.executeFunction(scriptId, functionName, params);
//            return ResponseEntity.ok(Map.of("result", result));
            return ResponseEntity.ok(new HashMap<>().put("result", result));
        } catch (Exception e) {
            log.error("执行函数失败: {}", e.getMessage(), e);
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            return ResponseEntity.badRequest().body(new HashMap<>().put("message", e.getMessage()));
        }
    }


}
