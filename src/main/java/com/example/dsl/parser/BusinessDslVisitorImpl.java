package com.example.dsl.parser;

import com.example.dsl.BusinessDslBaseVisitor;
import com.example.dsl.BusinessDslParser;
import com.example.model.DslFunction;
import com.example.model.DslScript;
import com.example.dsl.runtime.DslContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class BusinessDslVisitorImpl extends BusinessDslBaseVisitor<Object> {

    private final DslScript script;
    private final DslContext context;

    // return值与标志
    private Object returnValue = null;
    private boolean hasReturn = false;

    public BusinessDslVisitorImpl(String scriptName) {
        script = new DslScript();
        script.setName(scriptName);
        script.setCreatedAt(LocalDateTime.now());
        script.setUpdatedAt(LocalDateTime.now());
        script.setEnabled(true);
        context = new DslContext(); // 创建全局上下文
    }

    // 便于测试和外部设置变量
    public void setVariable(String name, Object value) {
        context.setVariable(name, value);
    }

    public Object getVariable(String name) {
        return context.getVariable(name);
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

        // 存储函数体
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
        context.setVariable(varName, value);
        log.info("[Declare] {} = {}", varName, value);
        return null;
    }

    @Override
    public Object visitAssignmentStmt(BusinessDslParser.AssignmentStmtContext ctx) {
        String varName = ctx.ID().getText();
        Object value = visit(ctx.expr());
        context.setVariable(varName, value);
        log.info("[Assign] {} = {}", varName, value);
        return null;
    }

    // ========== if/for/return/块 ========== //
    @Override
    public Object visitIfStatement(BusinessDslParser.IfStatementContext ctx) {
        Object cond = visit(ctx.expr());
        log.info("[If] condition: {} => {}", ctx.expr().getText(), cond);
        if (toBoolean(cond)) {
            context.enterScope();
            Object result = visit(ctx.block());
            context.exitScope();
            return result;
        } else if (ctx.elseIfBlock() != null) {
            context.enterScope();
            Object result = visit(ctx.elseIfBlock());
            context.exitScope();
            return result;
        }
        return null;
    }

    @Override
    public Object visitMemberExpr(BusinessDslParser.MemberExprContext ctx) {
        Object obj = visit(ctx.expr());
        String member = ctx.ID().getText();
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(member);
        }
        throw new RuntimeException("Cannot access member '" + member + "' of non-object: " + obj);
    }

    @Override
    public Object visitForStatement(BusinessDslParser.ForStatementContext ctx) {
        Object iterable = visit(ctx.expr());
        if (iterable instanceof List) {
            List<?> list = (List<?>) iterable;
            for (int i = 0; i < list.size(); i++) {
                context.enterScope();
                // 设置循环变量为当前索引
                context.setVariable(ctx.ID().getText(), i);
                // 设置当前元素值
                context.setVariable("current", list.get(i));
                visit(ctx.block());
                context.exitScope();
                if (hasReturn) break;
            }
        } else if (iterable instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) iterable).entrySet()) {
                context.enterScope();
                context.setVariable(ctx.ID().getText(), entry.getKey());
                context.setVariable("current", entry.getValue());
                visit(ctx.block());
                context.exitScope();
                if (hasReturn) break;
            }
        }
        return null;
    }

    @Override
    public Object visitIndexExpr(BusinessDslParser.IndexExprContext ctx) {
        Object collection = visit(ctx.expr(0));
        Object index = visit(ctx.expr(1));
        if (collection instanceof List && index instanceof Number) {
            int idx = ((Number) index).intValue();
            List<?> list = (List<?>) collection;
            if (idx >= 0 && idx < list.size()) {
                return list.get(idx);
            }
            throw new RuntimeException("Array index out of bounds: " + idx);
        } else if (collection instanceof Map) {
            return ((Map<?, ?>) collection).get(index);
        }
        throw new RuntimeException("Invalid array/map access: " + collection + "[" + index + "]");
    }

    @Override
    public Object visitReturnStatement(BusinessDslParser.ReturnStatementContext ctx) {
        returnValue = ctx.expr() != null ? visit(ctx.expr()) : null;
        hasReturn = true;
        log.info("[Return] value: {}", returnValue);
        return returnValue;
    }

    @Override
    public Object visitBlock(BusinessDslParser.BlockContext ctx) {
        context.enterScope();
        Object last = null;
        for (BusinessDslParser.StatementContext stmt : ctx.statement()) {
            last = visit(stmt);
            if (hasReturn) break;
        }
        context.exitScope();
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

        // 先检查是否是内置函数
        com.example.dsl.runtime.DslFunction dslFunction = context.getGlobalFunction(funcName);
        if (dslFunction != null) {
            return dslFunction.execute(args.toArray());
        }

        // 检查是否是业务函数
        if ("checkVipStatus".equals(funcName)) {
            return checkVipStatus(args.get(0));
        }
        if ("checkSeason".equals(funcName)) {
            return checkSeason(args.get(0));
        }

        // 用户自定义函数
        DslFunction function = script.getFunction(funcName);
        if (function != null) {
            DslContext functionContext = context.createChildContext();
            // 设置参数
            for (int i = 0; i < function.getParameters().size(); i++) {
                functionContext.setVariable(function.getParameters().get(i), args.get(i));
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

        // 处理数值运算
        if (left instanceof Number && right instanceof Number) {
            double l = ((Number) left).doubleValue();
            double r = ((Number) right).doubleValue();
            return "+".equals(op) ? l + r : l - r;
        }

        // 处理字符串拼接
        if ("+".equals(op) && (left instanceof String || right instanceof String)) {
            return String.valueOf(left) + String.valueOf(right);
        }

        throw new RuntimeException("Invalid operands for " + op + ": " + left + ", " + right);
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
        String id = ctx.ID().getText();
        return context.getVariable(id);
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
        boolean result;
        if (op.equals("==")) {
            if (left == null) result = (right == null);
            else if (left instanceof String && right instanceof String) result = left.equals(right);
            else result = left.equals(right);
        } else if (op.equals("!=")) {
            if (left == null) result = (right != null);
            else if (left instanceof String && right instanceof String) result = !left.equals(right);
            else result = !left.equals(right);
        } else {
            result = false;
        }
        log.info("[EqualityExpr] {} {} {} => {}", left, op, right, result);
        return result;
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
        if (!toBoolean(left)) return false; // 短路求值
        Object right = visit(ctx.expr(1));
        return toBoolean(right);
    }

    @Override
    public Object visitOrExpr(BusinessDslParser.OrExprContext ctx) {
        Object left = visit(ctx.expr(0));
        if (toBoolean(left)) return true; // 短路求值
        Object right = visit(ctx.expr(1));
        return toBoolean(right);
    }

    @Override
    public Object visitArrayExpr(BusinessDslParser.ArrayExprContext ctx) {
        List<Object> list = new ArrayList<>();
        if (ctx.array().expr() != null) {
            for (BusinessDslParser.ExprContext exprCtx : ctx.array().expr()) {
                list.add(visit(exprCtx));
            }
        }
        return list;
    }

    @Override
    public Object visitObjectExpr(BusinessDslParser.ObjectExprContext ctx) {
        Map<String, Object> map = new HashMap<>();
        if (ctx.object().pair() != null) {
            for (BusinessDslParser.PairContext pairCtx : ctx.object().pair()) {
                String key;
                if (pairCtx.STRING() != null) {
                    String text = pairCtx.STRING().getText();
                    key = text.substring(1, text.length() - 1);
                } else {
                    key = pairCtx.ID().getText();
                }
                Object value = visit(pairCtx.expr());
                map.put(key, value);
            }
        }
        return map;
    }

    // 辅助方法：将对象转为布尔值
    private boolean toBoolean(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) return (Boolean) obj;
        if (obj instanceof Number) return ((Number) obj).doubleValue() != 0;
        if (obj instanceof String) return !((String) obj).isEmpty();
        if (obj instanceof Collection) return !((Collection<?>) obj).isEmpty();
        if (obj instanceof Map) return !((Map<?, ?>) obj).isEmpty();
        return true;
    }

    // ========== AST构建（可选，示例） ========== //
    // 你可以在每个visit方法里构建AST节点并返回
    // 这里只留接口，具体AST节点类可自定义
    // public AstNode visitVariableDecl(...) { ... return new VarDeclNode(...); }

    // ========== 内置函数实现 ========== //
    private boolean checkVipStatus(Object userId) {
        // 模拟VIP检查逻辑
        return userId != null && userId.toString().startsWith("VIP");
    }
    private boolean checkSeason(Object season) {
        // 模拟季节检查逻辑
        return "summer".equals(season) || "winter".equals(season);
    }

    @Override
    protected Object aggregateResult(Object aggregate, Object nextResult) {
        return nextResult != null ? nextResult : aggregate;
    }
}
