package com.example.shopapp.vo;

import com.example.shopapp.entity.Promotion;

import java.math.BigDecimal;

/**
 * 限时特价活动 VO（接口文档 6.2.1）：活动信息 + 商品名称、主图、原价。
 */
public class PromotionVO extends Promotion {

    private String productName;
    private String productImage;
    private BigDecimal originalPrice; // 商品原价

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }
}
