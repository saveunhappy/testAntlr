package com.example.dsl.parser;

import com.example.dsl.BusinessDslBaseVisitor;
import com.example.dsl.BusinessDslParser;
import com.example.model.DslFunction;
import com.example.model.DslScript;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.ParseTree;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class BusinessDslVisitorImpl extends BusinessDslBaseVisitor<Object> {

    private final DslScript script;
    // 变量作用域（简单实现，支持嵌套函数时可用栈优化）
    private final Map<String, Object> variables = new HashMap<>();
    // return值与标志
    private Object returnValue = null;
    private boolean hasReturn = false;

    // 便于测试和外部设置变量
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }
    public Object getVariable(String name) {
        return variables.get(name);
    }

    public BusinessDslVisitorImpl(String scriptName) {
        script = new DslScript();
        script.setName(scriptName);
        script.setCreatedAt(LocalDateTime.now());
        script.setUpdatedAt(LocalDateTime.now());
        script.setEnabled(true);
    }

    @Override
    public DslScript visitProgram(BusinessDslParser.ProgramContext ctx) {
        // 访问所有语句
        ctx.statement().forEach(this::visit);
        return script;
    }

    @Override
    public Object visitFunctionDecl(BusinessDslParser.FunctionDeclContext ctx) {
        String functionName = ctx.ID().getText();
        DslFunction function = new DslFunction(functionName);

        // 处理参数列表
        if (ctx.paramList() != null) {
            ctx.paramList().ID().forEach(id -> function.addParameter(id.getText()));
        }

        // 存储函数体（这里简单存储上下文，实际应用中可能需要更复杂的处理）
        function.setBody(ctx.block());

        // 添加到脚本
        script.addFunction(function);
        log.info("解析到函数: {}", functionName);

        return function;
    }

    // ========== 变量声明与赋值 ========== //
    @Override
    public Object visitVariableDecl(BusinessDslParser.VariableDeclContext ctx) {
        String varName = ctx.ID().getText();
        Object value = ctx.expr() != null ? visit(ctx.expr()) : null;
        variables.put(varName, value);
        // AST: 可返回自定义AST节点
        return null;
    }

    @Override
    public Object visitAssignmentStmt(BusinessDslParser.AssignmentStmtContext ctx) {
        String varName = ctx.ID().getText();
        Object value = visit(ctx.expr());
        variables.put(varName, value);
        // AST: 可返回自定义AST节点
        return null;
    }

    // ========== if/for/return/块 ========== //
    @Override
    public Object visitIfStatement(BusinessDslParser.IfStatementContext ctx) {
        Object cond = visit(ctx.expr());
        if (Boolean.TRUE.equals(cond)) {
            visit(ctx.block());
        } else if (ctx.elseIfBlock() != null) {
            visit(ctx.elseIfBlock());
        }
        return null;
    }

    @Override
    public Object visitForStatement(BusinessDslParser.ForStatementContext ctx) {
        String varName = ctx.ID().getText();
        Object iterable = visit(ctx.expr());
        if (iterable instanceof Iterable) {
            for (Object item : (Iterable<?>) iterable) {
                variables.put(varName, item);
                visit(ctx.block());
                if (hasReturn) break;
            }
        }
        return null;
    }

    @Override
    public Object visitReturnStatement(BusinessDslParser.ReturnStatementContext ctx) {
        returnValue = ctx.expr() != null ? visit(ctx.expr()) : null;
        hasReturn = true;
        return returnValue;
    }

    @Override
    public Object visitBlock(BusinessDslParser.BlockContext ctx) {
        Object last = null;
        for (BusinessDslParser.StatementContext stmt : ctx.statement()) {
            last = visit(stmt);
            if (hasReturn) break;
        }
        return hasReturn ? returnValue : last;
    }

    // ========== 函数调用 ========== //
    @Override
    public Object visitFunctionCall(BusinessDslParser.FunctionCallContext ctx) {
        String funcName = ctx.ID().getText();
        List<Object> args = new ArrayList<>();
        if (ctx.argumentList() != null) {
            for (BusinessDslParser.ExprContext exprCtx : ctx.argumentList().expr()) {
                args.add(visit(exprCtx));
            }
        }
        // 内置函数
        if ("checkVipStatus".equals(funcName)) {
            return checkVipStatus(args.get(0));
        }
        if ("checkSeason".equals(funcName)) {
            return checkSeason(args.get(0));
        }
        // 用户自定义函数
        DslFunction function = script.getFunction(funcName);
        if (function != null) {
            Map<String, Object> oldVars = new HashMap<>(variables);
            for (int i = 0; i < function.getParameters().size(); i++) {
                variables.put(function.getParameters().get(i), args.get(i));
            }
            hasReturn = false;
            returnValue = null;
            Object result = null;
            if (function.getBody() instanceof org.antlr.v4.runtime.tree.ParseTree) {
                result = visit((org.antlr.v4.runtime.tree.ParseTree) function.getBody());
            }
            Object ret = hasReturn ? returnValue : result;
            hasReturn = false;
            returnValue = null;
            variables.clear();
            variables.putAll(oldVars);
            return ret;
        }
        return null;
    }

    // ========== 表达式求值 ========== //
    @Override
    public Object visitAddSubExpr(BusinessDslParser.AddSubExprContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        String op = ctx.op.getText();
        if (left instanceof Number && right instanceof Number) {
            double l = ((Number) left).doubleValue();
            double r = ((Number) right).doubleValue();
            return "+".equals(op) ? l + r : l - r;
        }
        return null;
    }

    @Override
    public Object visitMulDivExpr(BusinessDslParser.MulDivExprContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        String op = ctx.op.getText();
        if (left instanceof Number && right instanceof Number) {
            double l = ((Number) left).doubleValue();
            double r = ((Number) right).doubleValue();
            switch (op) {
                case "*": return l * r;
                case "/": return l / r;
                case "%": return l % r;
            }
        }
        return null;
    }

    @Override
    public Object visitIdExpr(BusinessDslParser.IdExprContext ctx) {
        String varName = ctx.ID().getText();
        return variables.get(varName);
    }

    @Override
    public Object visitNumberExpr(BusinessDslParser.NumberExprContext ctx) {
        return Double.valueOf(ctx.NUMBER().getText());
    }

    @Override
    public Object visitStringExpr(BusinessDslParser.StringExprContext ctx) {
        String text = ctx.STRING().getText();
        return text.substring(1, text.length() - 1);
    }

    @Override
    public Object visitParenExpr(BusinessDslParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Object visitEqualityExpr(BusinessDslParser.EqualityExprContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        String op = ctx.op.getText();
        if (left == null || right == null) return false;
        if (op.equals("==")) return left.equals(right);
        if (op.equals("!=")) return !left.equals(right);
        return false;
    }

    @Override
    public Object visitComparisonExpr(BusinessDslParser.ComparisonExprContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        String op = ctx.op.getText();
        if (left instanceof Number && right instanceof Number) {
            double l = ((Number) left).doubleValue();
            double r = ((Number) right).doubleValue();
            switch (op) {
                case "<": return l < r;
                case ">": return l > r;
                case "<=": return l <= r;
                case ">=": return l >= r;
            }
        }
        if (left instanceof String && right instanceof String) {
            int cmp = ((String) left).compareTo((String) right);
            switch (op) {
                case "<": return cmp < 0;
                case ">": return cmp > 0;
                case "<=": return cmp <= 0;
                case ">=": return cmp >= 0;
            }
        }
        return false;
    }

    @Override
    public Object visitBooleanExpr(BusinessDslParser.BooleanExprContext ctx) {
        return Boolean.valueOf(ctx.BOOLEAN().getText());
    }

    @Override
    public Object visitNullExpr(BusinessDslParser.NullExprContext ctx) {
        return null;
    }

    @Override
    public Object visitAndExpr(BusinessDslParser.AndExprContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        return toBoolean(left) && toBoolean(right);
    }

    @Override
    public Object visitOrExpr(BusinessDslParser.OrExprContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        return toBoolean(left) || toBoolean(right);
    }

    // 辅助方法：将对象转为布尔值
    private boolean toBoolean(Object obj) {
        if (obj instanceof Boolean) return (Boolean) obj;
        if (obj instanceof Number) return ((Number) obj).doubleValue() != 0.0;
        if (obj instanceof String) return !((String) obj).isEmpty();
        return obj != null;
    }

    // ========== AST构建（可选，示例） ========== //
    // 你可以在每个visit方法里构建AST节点并返回
    // 这里只留接口，具体AST节点类可自定义
    // public AstNode visitVariableDecl(...) { ... return new VarDeclNode(...); }

    // ========== 内置函数实现 ========== //
    private boolean checkVipStatus(Object userId) {
        if (userId == null) return false;
        String uid = userId.toString();
        return uid.startsWith("VIP");
    }
    private boolean checkSeason(Object season) {
        // 假设当前季节是summer
        return "summer".equals(season);
    }

    @Override
    protected Object aggregateResult(Object aggregate, Object nextResult) {
        return nextResult != null ? nextResult : aggregate;
    }
}
