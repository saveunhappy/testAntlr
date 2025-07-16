package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面控制器
 * 提供Web页面的访问
 */
@Controller
public class PageController {

    /**
     * DSL编辑器页面
     */
    @GetMapping("/")
    public String dslEditor() {
        return "dsl-editor";
    }

    /**
     * DSL编辑器页面（别名）
     */
//    @GetMapping("/editor")
//    public String editor() {
//        return "dsl-editor";
//    }
}
