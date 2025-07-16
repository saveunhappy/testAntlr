package com.example.dsl;

import com.example.dsl.parser.BusinessDslVisitorImpl;
import com.example.dsl.parser.DslParser;
import com.example.model.DslScript;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DslParserTest {
    private String readDsl(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
    @Test
    public void testCalculateDiscount() throws Exception {
        String dsl = readDsl("scripts/discount.dsl");
        DslParser parser = new DslParser();
        DslScript script = parser.parse("discount.dsl", dsl);
        BusinessDslVisitorImpl visitor = new BusinessDslVisitorImpl("discount.dsl");
        // 模拟调用 calculateDiscount("PROD001", 100, "VIP123")
        visitor.setVariable("productId", "PROD001");
        visitor.setVariable("price", 100.0);
        visitor.setVariable("userId", "VIP123");
        Object result = visitor.visit((org.antlr.v4.runtime.tree.ParseTree) script.getFunction("calculateDiscount").getBody());
        System.out.println("discount result: " + result);
        assertEquals(80.0, result); // PROD001 20%折扣，VIP更低，最终80
    }

    @Test
    public void testCalculatePrice() throws Exception {
        String dsl = readDsl("scripts/pricing.dsl");
        DslParser parser = new DslParser();
        DslScript script = parser.parse("pricing.dsl", dsl);
        BusinessDslVisitorImpl visitor = new BusinessDslVisitorImpl("pricing.dsl");
        // 构造一个商品对象
        Map<String, Object> product = new HashMap<>();
        product.put("id", "P001");
        product.put("basePrice", 1200.0);
        product.put("category", "electronics");
        product.put("stock", 5);
        product.put("season", "summer");
        visitor.setVariable("product", product);
        Object result = visitor.visit((org.antlr.v4.runtime.tree.ParseTree) script.getFunction("calculatePrice").getBody());
        System.out.println("price result: " + result);
        assertNotNull(result);
    }
}
