package com.example.shopapp.req;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建订单请求（接口文档 8.3）。
 */
public class CreateOrderReq implements Serializable {

    private Long addressId;                          // 收货地址 ID，必填
    private List<OrderItemReq> items = new ArrayList<>(); // 购买商品列表，必填
    private Long userCouponId;                       // 使用的用户优惠券 ID，可选
    private String remark;                           // 订单备注
    private Boolean fromCart;                        // true 表示来自购物车结算，成功后删除对应购物车记录

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public List<OrderItemReq> getItems() {
        return items;
    }

    public void setItems(List<OrderItemReq> items) {
        this.items = items;
    }

    public Long getUserCouponId() {
        return userCouponId;
    }

    public void setUserCouponId(Long userCouponId) {
        this.userCouponId = userCouponId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getFromCart() {
        return fromCart;
    }

    public void setFromCart(Boolean fromCart) {
        this.fromCart = fromCart;
    }

    public boolean isFromCart() {
        return fromCart != null && fromCart;
    }
}
