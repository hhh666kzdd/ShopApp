package com.example.shopapp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单 VO（接口文档 8.11）。
 */
public class OrderVO implements Serializable {

    // 订单状态（接口文档 8.1）
    public static final int STATUS_UNPAID = 0;    // 待付款
    public static final int STATUS_PAID = 1;      // 待发货
    public static final int STATUS_SHIPPED = 2;   // 待收货
    public static final int STATUS_FINISHED = 3;  // 已完成
    public static final int STATUS_CANCELED = 4;  // 已取消

    private Long id;
    private String orderNo;
    private Long userId;
    private String username;          // 下单用户名（后台列表使用）
    private Integer status;
    private BigDecimal totalAmount;   // 商品总额
    private BigDecimal discountAmount;// 优惠金额
    private BigDecimal payAmount;     // 实付金额
    private Long userCouponId;        // 使用的优惠券，未使用为 null
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;   // 收货地址快照
    private String remark;
    private String payTime;
    private String shipTime;
    private String finishTime;
    private String createTime;
    private List<OrderItemVO> items = new ArrayList<>();

    /** 状态文字：待付款 / 待发货 / 待收货 / 已完成 / 已取消 */
    public String getStatusText() {
        return statusText(status);
    }

    public static String statusText(Integer status) {
        if (status == null) return "";
        switch (status) {
            case STATUS_UNPAID:
                return "待付款";
            case STATUS_PAID:
                return "待发货";
            case STATUS_SHIPPED:
                return "待收货";
            case STATUS_FINISHED:
                return "已完成";
            case STATUS_CANCELED:
                return "已取消";
            default:
                return "未知";
        }
    }

    /** 订单内商品总件数 */
    public int getTotalQuantity() {
        int n = 0;
        for (OrderItemVO item : items) {
            n += item.getQuantity() == null ? 0 : item.getQuantity();
        }
        return n;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Long getUserCouponId() {
        return userCouponId;
    }

    public void setUserCouponId(Long userCouponId) {
        this.userCouponId = userCouponId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getShipTime() {
        return shipTime;
    }

    public void setShipTime(String shipTime) {
        this.shipTime = shipTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public List<OrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVO> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }
}
