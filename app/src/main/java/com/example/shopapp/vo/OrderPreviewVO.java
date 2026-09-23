package com.example.shopapp.vo;

import com.example.shopapp.entity.Address;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单预览 VO（接口文档 8.2），结算页展示用。
 */
public class OrderPreviewVO implements Serializable {

    private List<OrderItemVO> items = new ArrayList<>(); // 商品明细（按现价计算）
    private Address address;                             // 收货地址，可为 null
    private UserCouponVO coupon;                         // 已选优惠券，可为 null
    private BigDecimal totalAmount;                      // 商品总额
    private BigDecimal discountAmount;                   // 优惠金额
    private BigDecimal payAmount;                        // 应付金额 = totalAmount − discountAmount

    public List<OrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVO> items) {
        this.items = items;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public UserCouponVO getCoupon() {
        return coupon;
    }

    public void setCoupon(UserCouponVO coupon) {
        this.coupon = coupon;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }
}
