package com.example.shopapp.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车已勾选合计（接口文档 5.8）。
 */
public class CartSummary implements Serializable {

    private int totalCount;          // 勾选商品数量（件数）
    private BigDecimal totalAmount;  // 勾选商品按现价合计

    public CartSummary() {
        this.totalCount = 0;
        this.totalAmount = BigDecimal.ZERO;
    }

    public CartSummary(int totalCount, BigDecimal totalAmount) {
        this.totalCount = totalCount;
        this.totalAmount = totalAmount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
