/*
 * 折扣计算DSL脚本
 * 根据产品ID、价格和用户ID计算折扣
 */
function calculateDiscount(productId, price, userId) {
    // VIP用户折扣
    var vipDiscount = 0.9; // 10%折扣
    
    // 特定产品折扣
    var productDiscount = 1.0;
    if (productId == "PROD001") {
        productDiscount = 0.8; // 20%折扣
    } else if (productId == "PROD002") {
        productDiscount = 0.85; // 15%折扣
    }
    
    // 判断是否是VIP用户
    var isVip = checkVipStatus(userId);
    
    // 计算最终折扣
    var finalDiscount = isVip ? vipDiscount : 1.0;
    finalDiscount = finalDiscount < productDiscount ? finalDiscount : productDiscount;
    
    // 返回折扣后价格
    return price * finalDiscount;
}

// 检查用户VIP状态
function checkVipStatus(userId) {
    // 这里可以调用平台提供的UDF函数
    // 简化示例，假设用户ID以VIP开头的是VIP用户
    return userId != null && userId.indexOf("VIP") == 0;
}