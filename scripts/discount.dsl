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
    var finalDiscount = 1.0;
    if (isVip) {
        finalDiscount = vipDiscount;
    }
    if (finalDiscount < productDiscount) {
        // 保持finalDiscount
    } else {
        finalDiscount = productDiscount;
    }

    // 返回折扣后价格
    return price * finalDiscount;
}
