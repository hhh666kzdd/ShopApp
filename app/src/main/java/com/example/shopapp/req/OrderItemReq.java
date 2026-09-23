package com.example.shopapp.req;

import java.io.Serializable;

/**
 * 下单商品项（接口文档 8.2）。
 */
public class OrderItemReq implements Serializable {

    private Long productId;   // 商品 ID
    private Integer quantity; // 购买数量

    public OrderItemReq() {
    }

    public OrderItemReq(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
