package com.example.dsl.runtime;

import com.example.dsl.parser.DslParser;
import com.example.model.DslScript;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DslScriptLoader {

    private final DslParser dslParser;
    private final DslEngine dslEngine;

    private final Map<String, Long> scriptLastModified = new HashMap<>();
    private final Path scriptsDirectory = Paths.get("scripts");

    // 初始化脚本目录
    public void init() {
        try {
            if (!Files.exists(scriptsDirectory)) {
                Files.createDirectories(scriptsDirectory);
                log.info("创建脚本目录: {}", scriptsDirectory);
            }

            // 加载所有脚本
            loadAllScripts();
        } catch (IOException e) {
            log.error("初始化脚本目录失败: {}", e.getMessage(), e);
        }
    }

    // 加载所有脚本
    public void loadAllScripts() {
        try {
            Files.list(scriptsDirectory)
                    .filter(path -> path.toString().endsWith(".dsl"))
                    .forEach(this::loadScript);
        } catch (IOException e) {
            log.error("加载脚本失败: {}", e.getMessage(), e);
        }
    }

    // 加载单个脚本
    public Object loadScript(Path path) {
        try {
            String scriptId = path.getFileName().toString();
            // 替换 Files.readString(path)
            byte[] fileBytes = Files.readAllBytes(path);
            String content = new String(fileBytes, StandardCharsets.UTF_8);
            long lastModified = Files.getLastModifiedTime(path).toMillis();

            // 解析脚本
            Object result = dslParser.parse(scriptId, content);

            // 如果是DslScript，加载到引擎
            if (result instanceof DslScript) {
                DslScript script = (DslScript) result;
                dslEngine.loadScript(script);
                // 记录最后修改时间
                scriptLastModified.put(scriptId, lastModified);
                log.info("加载脚本: {}", scriptId);
            }

            return result;
        } catch (IOException e) {
            log.error("读取脚本文件失败: {}", e.getMessage(), e);
            return null;
        }
    }

    // 保存脚本
//    public void saveScript(String scriptId, String content) throws IOException {
//        Path scriptPath = scriptsDirectory.resolve(scriptId);
//        Files.writeString(scriptPath, content);
//        log.info("保存脚本: {}", scriptId);
//
//        // 重新加载脚本
//        loadScript(scriptPath);
//    }
    public void saveScript(String scriptId, String content) throws IOException {
        Path scriptPath = scriptsDirectory.resolve(scriptId);

        // Java 8 替代 Files.writeString()
        Files.write(scriptPath, content.getBytes(StandardCharsets.UTF_8));
        log.info("保存脚本: {}", scriptId);

        // 重新加载脚本
        loadScript(scriptPath);
    }

    // 定时检查脚本更新（热加载）
    @Scheduled(fixedDelay = 5000) // 每5秒检查一次
    public void checkScriptUpdates() {
        try {
            Files.list(scriptsDirectory)
                    .filter(path -> path.toString().endsWith(".dsl"))
                    .forEach(path -> {
                        try {
                            String scriptId = path.getFileName().toString();
                            long lastModified = Files.getLastModifiedTime(path).toMillis();

                            // 检查是否有更新
                            if (!scriptLastModified.containsKey(scriptId) ||
                                    scriptLastModified.get(scriptId) < lastModified) {
                                log.info("检测到脚本更新: {}", scriptId);
                                loadScript(path);
                            }
                        } catch (IOException e) {
                            log.error("检查脚本更新失败: {}", e.getMessage(), e);
                        }
                    });
        } catch (IOException e) {
            log.error("检查脚本目录失败: {}", e.getMessage(), e);
        }
    }
}
