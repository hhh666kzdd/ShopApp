package com.example.shopapp.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售统计 VO（接口文档 9.4）。
 */
public class StatisticsVO implements Serializable {

    private Integer userCount;        // 用户总数
    private Integer productCount;     // 上架商品数
    private Integer orderCount;       // 订单总数（已支付有效订单）
    private Integer todayOrderCount;  // 今日订单数
    private Integer pendingShipCount; // 待发货订单数
    private BigDecimal totalSales;    // 累计销售额
    private BigDecimal todaySales;    // 今日销售额

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public Integer getTodayOrderCount() {
        return todayOrderCount;
    }

    public void setTodayOrderCount(Integer todayOrderCount) {
        this.todayOrderCount = todayOrderCount;
    }

    public Integer getPendingShipCount() {
        return pendingShipCount;
    }

    public void setPendingShipCount(Integer pendingShipCount) {
        this.pendingShipCount = pendingShipCount;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getTodaySales() {
        return todaySales;
    }

    public void setTodaySales(BigDecimal todaySales) {
        this.todaySales = todaySales;
    }
}
