package com.example.controller;

import com.example.dsl.runtime.DslEngine;
import com.example.model.DslScript;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class EditorController {

    private final DslEngine dslEngine;

    /**
     * 编辑器主页
     */
    @GetMapping("/editor")
    public String editorHome(Model model) {
        model.addAttribute("scripts", dslEngine.getLoadedScripts().values());
        return "index";
    }

    /**
     * 编辑特定脚本
     */
    @GetMapping("/editor/{scriptId}")
    public String editScript(@PathVariable String scriptId, Model model) {
        DslScript script = dslEngine.getLoadedScripts().get(scriptId);
        if (script == null) {
            // 创建新脚本
            script = new DslScript();
            script.setId(scriptId);
            script.setName(scriptId);
            script.setContent("");
        }

        model.addAttribute("script", script);
        return "edit";
    }

    /**
     * 调试工具
     */
    @GetMapping("/debug/{scriptId}")
    public String debugScript(@PathVariable String scriptId, Model model) {
        DslScript script = dslEngine.getLoadedScripts().get(scriptId);
        if (script == null) {
            return "redirect:/editor";
        }

        model.addAttribute("script", script);
        return "debug";
    }
}
