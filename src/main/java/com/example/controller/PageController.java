package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面路由控制器
 */
@Controller
public class PageController {
    
    /**
     * DSL编辑器页面
     */
    @GetMapping("/dsl-editor")
    public String editor() {
        return "dsl-editor";
    }
}
