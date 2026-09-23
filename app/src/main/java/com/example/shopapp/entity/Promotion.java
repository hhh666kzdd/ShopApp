package com.example.shopapp.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 限时特价活动实体，对应 t_promotion 表；作为管理员新增 / 修改活动的入参（接口文档 6.2.3）。
 */
public class Promotion implements Serializable {

    private Long id;
    private String title;          // 活动标题
    private String description;    // 活动说明
    private Long productId;        // 活动商品
    private BigDecimal promoPrice; // 活动价，必须低于商品原价
    private String startTime;
    private String endTime;
    private Integer status;        // 1 启用，0 停用

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(BigDecimal promoPrice) {
        this.promoPrice = promoPrice;
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
