package com.example.shopapp.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品 VO（接口文档 4.9），在商品实体基础上附带分类名称和进行中的活动价。
 */
public class ProductVO implements Serializable {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subtitle;
    private String mainImage;
    private String detail;
    private BigDecimal price;          // 原价
    private BigDecimal promoPrice;     // 活动价，无活动为 null
    private String promotionTitle;     // 活动名称，无活动为 null
    private Integer stock;
    private Integer sales;
    private Integer status;            // 1 上架，0 下架
    private String createTime;

    /** 现价：有活动取活动价，否则取原价（接口文档 1.7） */
    public BigDecimal getCurrentPrice() {
        return promoPrice != null ? promoPrice : price;
    }

    public boolean hasPromotion() {
        return promoPrice != null;
    }

    public boolean isOnSale() {
        return status != null && status == 1;
    }

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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public BigDecimal getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(BigDecimal promoPrice) {
        this.promoPrice = promoPrice;
    }

    public String getPromotionTitle() {
        return promotionTitle;
    }

    public void setPromotionTitle(String promotionTitle) {
        this.promotionTitle = promotionTitle;
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
