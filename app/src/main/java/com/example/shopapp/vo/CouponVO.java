package com.example.shopapp.vo;

import com.example.shopapp.entity.Coupon;

import java.math.BigDecimal;

/**
 * 优惠券 VO（接口文档 6.1.1）：优惠券信息 + 当前用户是否已领取。
 */
public class CouponVO extends Coupon {

    private Boolean received; // 当前用户是否已领取

    public Boolean getReceived() {
        return received;
    }

    public void setReceived(Boolean received) {
        this.received = received;
    }

    public boolean isReceived() {
        return received != null && received;
    }

    /** 优惠描述，如「满 200 减 30」「满 100 打 9 折」 */
    public String getDiscountText() {
        return buildDiscountText(getType(), getThreshold(), getDiscountAmount(), getDiscountRate());
    }

    /** 供 CouponVO / UserCouponVO 共用的优惠描述拼接 */
    public static String buildDiscountText(Integer type, BigDecimal threshold,
                                           BigDecimal discountAmount, BigDecimal discountRate) {
        boolean noThreshold = threshold == null || threshold.compareTo(BigDecimal.ZERO) <= 0;
        String prefix = noThreshold ? "无门槛" : "满" + threshold.stripTrailingZeros().toPlainString() + "元";
        if (type != null && type == TYPE_RATE) {
            // 0.90 -> 9 折，0.85 -> 8.5 折
            BigDecimal zhe = discountRate == null ? BigDecimal.TEN
                    : discountRate.multiply(BigDecimal.TEN).stripTrailingZeros();
            return prefix + "打" + zhe.toPlainString() + "折";
        }
        String amount = discountAmount == null ? "0" : discountAmount.stripTrailingZeros().toPlainString();
        return prefix + "减" + amount + "元";
    }
}
