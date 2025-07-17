package com.example.dsl.parser;

import com.example.dsl.BusinessDslLexer;
import com.example.dsl.BusinessDslParser;
import com.example.model.DslScript;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DslParser {

    public Object parse(String scriptName, String content) {
        try {
            // 创建词法分析器
            BusinessDslLexer lexer = new BusinessDslLexer(CharStreams.fromString(content));

            // 创建词法符号流
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // 创建语法分析器
            BusinessDslParser parser = new BusinessDslParser(tokens);

            // 获取解析树
            ParseTree tree = parser.program();

            // 创建访问者并访问解析树
            BusinessDslVisitorImpl visitor = new BusinessDslVisitorImpl(scriptName);
            
            // 如果是执行模式，设置执行标志
            if (Boolean.TRUE.equals(visitor.getVariable("__EXECUTE_MODE__"))) {
                return visitor.visit(tree);
            } else {
                // 如果是解析模式，返回脚本对象
                DslScript script = (DslScript) visitor.visit(tree);
                script.setContent(content);
                return script;
            }
        } catch (Exception e) {
            log.error("解析DSL脚本失败: {}", e.getMessage(), e);
            throw new RuntimeException("解析DSL脚本失败: " + e.getMessage(), e);
        }
    }
}
