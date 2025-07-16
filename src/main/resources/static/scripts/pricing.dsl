/*
 * 定价策略DSL脚本
 * 根据产品信息计算最终价格
 */
function calculatePrice(product) {
    var basePrice = product["basePrice"];
    var category = product["category"];
    var stock = product["stock"];
    
    var result = {
        "productId": product["id"],
        "originalPrice": basePrice,
        "finalPrice": basePrice,
        "discountApplied": false,
        "discountReason": ""
    };
    
    // 根据库存调整价格
    if (stock < 10) {
        // 库存少，提高价格
        result["finalPrice"] = basePrice * 1.1;
        result["discountApplied"] = true;
        result["discountReason"] = "低库存溢价";
    } else if (stock > 100) {
        // 库存多，降低价格
        result["finalPrice"] = basePrice * 0.9;
        result["discountApplied"] = true;
        result["discountReason"] = "库存清仓";
    }
    
    // 根据类别应用特定规则
    if (category == "electronics") {
        // 电子产品利润率高，可以提供更多折扣
        if (basePrice > 1000) {
            result["finalPrice"] = result["finalPrice"] * 0.95;
            result["discountApplied"] = true;
            result["discountReason"] += ", 高价电子产品折扣";
        }
    } else if (category == "clothing") {
        // 服装类季节性强，根据季节调整价格
        var isSeason = checkSeason(product["season"]);
        if (!isSeason) {
            result["finalPrice"] = result["finalPrice"] * 0.7;
            result["discountApplied"] = true;
            result["discountReason"] += ", 非季节性服装折扣";
        }
    }
    
    return result;
}

// 检查是否是当前季节
function checkSeason(productSeason) {
    // 这里可以调用平台提供的UDF函数获取当前季节
    // 简化示例，假设当前是夏季
    var currentSeason = "summer";
    return productSeason == currentSeason;
}