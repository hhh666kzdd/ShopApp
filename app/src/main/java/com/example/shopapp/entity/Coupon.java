package com.example.shopapp.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠券实体，对应 t_coupon 表；作为管理员新增 / 修改优惠券的入参（接口文档 6.1.6）。
 */
public class Coupon implements Serializable {

    /** 满减券 */
    public static final int TYPE_CASH = 1;
    /** 折扣券 */
    public static final int TYPE_RATE = 2;

    private Long id;
    private String name;               // 优惠券名称
    private Integer type;              // 1 满减券，2 折扣券
    private BigDecimal threshold;      // 使用门槛，0 表示无门槛
    private BigDecimal discountAmount; // 满减金额（type=1）
    private BigDecimal discountRate;   // 折扣率，0.90 表示 9 折（type=2）
    private Integer total;             // 发放总量
    private Integer remain;            // 剩余数量
    private Integer perLimit;          // 每人限领张数
    private String startTime;          // 生效时间
    private String endTime;            // 失效时间
    private Integer status;            // 1 启用，0 停用

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getRemain() {
        return remain;
    }

    public void setRemain(Integer remain) {
        this.remain = remain;
    }

    public Integer getPerLimit() {
        return perLimit;
    }

    public void setPerLimit(Integer perLimit) {
        this.perLimit = perLimit;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
