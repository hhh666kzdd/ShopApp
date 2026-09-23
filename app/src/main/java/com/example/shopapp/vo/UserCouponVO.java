package com.example.shopapp.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户优惠券 VO（接口文档 6.1.3），下单时使用 id（用户优惠券 ID）。
 */
public class UserCouponVO implements Serializable {

    /** 未使用 */
    public static final int STATUS_UNUSED = 0;
    /** 已使用 */
    public static final int STATUS_USED = 1;
    /** 已过期 */
    public static final int STATUS_EXPIRED = 2;

    private Long id;                   // 用户优惠券 ID
    private Long couponId;             // 优惠券 ID
    private String name;
    private Integer type;              // 1 满减，2 折扣
    private BigDecimal threshold;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private Integer status;            // 0 未使用，1 已使用，2 已过期
    private String receiveTime;
    private String useTime;
    private String endTime;

    public String getDiscountText() {
        return CouponVO.buildDiscountText(type, threshold, discountAmount, discountRate);
    }

    public String getStatusText() {
        if (status == null) return "";
        switch (status) {
            case STATUS_UNUSED:
                return "未使用";
            case STATUS_USED:
                return "已使用";
            case STATUS_EXPIRED:
                return "已过期";
            default:
                return "";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getUseTime() {
        return useTime;
    }

    public void setUseTime(String useTime) {
        this.useTime = useTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
