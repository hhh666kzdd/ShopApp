package com.example.shopapp.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品实体，对应 t_product 表；作为管理员新增 / 修改商品的入参（接口文档 4.4）。
 */
public class Product implements Serializable {

    private Long id;
    private Long categoryId;    // 所属分类，必填
    private String name;        // 商品名称，必填
    private String subtitle;    // 副标题 / 卖点
    private String mainImage;   // 主图 URL
    private String detail;      // 商品描述
    private BigDecimal price;   // 原价，>0
    private Integer stock;      // 库存，>=0
    private Integer sales;      // 销量
    private Integer status;     // 1 上架，0 下架，默认 1
    private String createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
