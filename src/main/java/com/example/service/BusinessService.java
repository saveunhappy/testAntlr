package com.example.service;

import com.example.dsl.runtime.DslEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessService {

    private final DslEngine dslEngine;

    /**
     * 执行折扣计算
     * @param productId 产品ID
     * @param price 原价
     * @param userId 用户ID
     * @return 折扣后价格
     */
    public double calculateDiscount(String productId, double price, String userId) {
        try {
            // 调用DSL脚本中的折扣计算函数
            Object result = dslEngine.executeFunction("discount.dsl", "calculateDiscount", productId, price, userId);
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
            return price; // 默认返回原价
        } catch (Exception e) {
            log.error("计算折扣失败: {}", e.getMessage(), e);
            return price; // 出错时返回原价
        }
    }

    /**
     * 执行定价策略
     * @param product 产品信息
     * @return 定价结果
     */
    public Map<String, Object> calculatePrice(Map<String, Object> product) {
        try {
            // 调用DSL脚本中的定价策略函数
            Object result = dslEngine.executeFunction("pricing.dsl", "calculatePrice", product);
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            }
            return new HashMap<>(); // 默认返回空结果
        } catch (Exception e) {
            log.error("计算价格失败: {}", e.getMessage(), e);
            return new HashMap<>(); // 出错时返回空结果
        }
    }
}
