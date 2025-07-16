package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DSL脚本管理服务
 * 负责脚本的存储、加载、执行和热更新
 */
@Slf4j
@Service
public class DSLScriptService {

//    @Autowired
//    private DSLExecutionEngine executionEngine;

    private final Map<String, String> scriptCache = new ConcurrentHashMap<>();
    private final Path scriptsDir = Paths.get("scripts");
    private WatchService watchService;

    @PostConstruct
    public void init() {
        try {
            // 创建脚本目录
            if (!Files.exists(scriptsDir)) {
                Files.createDirectories(scriptsDir);
            }

            // 初始化文件监听
            initFileWatcher();

            // 加载现有脚本
            loadExistingScripts();

            log.info("DSL脚本服务初始化完成");
        } catch (IOException e) {
            log.error("初始化DSL脚本服务失败", e);
        }
    }

    /**
     * 初始化文件监听器
     */
    private void initFileWatcher() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        scriptsDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                           StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

        // 启动监听线程
        Thread watcherThread = new Thread(() -> {
            try {
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        Path fileName = (Path) event.context();
                        Path fullPath = scriptsDir.resolve(fileName);

                        if (kind == StandardWatchEventKinds.ENTRY_CREATE ||
                            kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            loadScript(fileName.toString());
                            log.info("脚本文件更新: {}", fileName);
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            scriptCache.remove(fileName.toString());
                            log.info("脚本文件删除: {}", fileName);
                        }
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                log.error("文件监听线程被中断", e);
            }
        });
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    /**
     * 加载现有脚本
     */
    private void loadExistingScripts() {
        try {
            Files.walk(scriptsDir, 1)
                .filter(path -> path.toString().endsWith(".dsl"))
                .forEach(path -> {
                    String fileName = path.getFileName().toString();
                    loadScript(fileName);
                });
        } catch (IOException e) {
            log.error("加载现有脚本失败", e);
        }
    }

    /**
     * 加载脚本文件
     */
    private void loadScript(String fileName) {
        try {
            Path scriptPath = scriptsDir.resolve(fileName);
            if (Files.exists(scriptPath)) {
                String content = new String(Files.readAllBytes(scriptPath));
                scriptCache.put(fileName, content);
                log.info("加载脚本: {}", fileName);
            }
        } catch (IOException e) {
            log.error("加载脚本失败: {}", fileName, e);
        }
    }

    /**
     * 保存脚本
     */
    public void saveScript(String scriptName, String content) {
        try {
            Path scriptPath = scriptsDir.resolve(scriptName + ".dsl");
            Files.write(scriptPath, content.getBytes());
            scriptCache.put(scriptName + ".dsl", content);
            log.info("保存脚本: {}", scriptName);
        } catch (IOException e) {
            log.error("保存脚本失败: {}", scriptName, e);
            throw new RuntimeException("保存脚本失败", e);
        }
    }

    /**
     * 获取脚本内容
     */
    public String getScript(String scriptName) {
        String fileName = scriptName.endsWith(".dsl") ? scriptName : scriptName + ".dsl";
        return scriptCache.get(fileName);
    }

    /**
     * 删除脚本
     */
    public void deleteScript(String scriptName) {
        try {
            String fileName = scriptName.endsWith(".dsl") ? scriptName : scriptName + ".dsl";
            Path scriptPath = scriptsDir.resolve(fileName);
            Files.deleteIfExists(scriptPath);
            scriptCache.remove(fileName);
            log.info("删除脚本: {}", scriptName);
        } catch (IOException e) {
            log.error("删除脚本失败: {}", scriptName, e);
            throw new RuntimeException("删除脚本失败", e);
        }
    }

    /**
     * 列出所有脚本
     */
    public Map<String, String> listScripts() {
        Map<String, String> scripts = new HashMap<>();
        scriptCache.forEach((fileName, content) -> {
            String scriptName = fileName.replace(".dsl", "");
            scripts.put(scriptName, content);
        });
        return scripts;
    }

    /**
     * 执行脚本
     */
//    public Object executeScript(String scriptName, Map<String, Object> context) {
//        String content = getScript(scriptName);
//        if (content == null) {
//            throw new DSLExecutionException("脚本不存在: " + scriptName);
//        }
//
//        return executionEngine.executeScript(scriptName, content, context);
//    }
//
//    /**
//     * 验证脚本语法
//     */
//    public boolean validateScript(String content) {
//        try {
//            executionEngine.executeScript("validation", content, new HashMap<>());
//            return true;
//        } catch (Exception e) {
//            log.warn("脚本语法验证失败: {}", e.getMessage());
//            return false;
//        }
//    }
}
