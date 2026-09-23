package com.example.shopapp.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车条目 VO（接口文档 5.1），联查商品表得到最新价格、库存、上架状态。
 */
public class CartItemVO implements Serializable {

    private Long id;                 // 购物车记录 ID
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;        // 原价
    private BigDecimal currentPrice; // 现价（含活动价）
    private Integer quantity;
    private Integer checked;         // 1 已勾选，0 未勾选
    private Integer stock;           // 当前库存
    private Integer status;          // 商品状态 1 上架 0 下架
    private BigDecimal subtotal;     // 小计 = currentPrice × quantity

    public boolean isChecked() {
        return checked != null && checked == 1;
    }

    /** 商品是否可购买：上架且有库存 */
    public boolean isAvailable() {
        return status != null && status == 1 && stock != null && stock > 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getChecked() {
        return checked;
    }

    public void setChecked(Integer checked) {
        this.checked = checked;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
