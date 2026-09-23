package com.example.shopapp.entity;

import java.io.Serializable;

/**
 * 收货地址，对应 t_address 表（接口文档 7.1）。
 */
public class Address implements Serializable {

    private Long id;
    private Long userId;
    private String receiverName;   // 收货人
    private String receiverPhone;  // 收货人手机号
    private String province;       // 省
    private String city;           // 市
    private String district;       // 区 / 县
    private String detail;         // 详细地址
    private Integer isDefault;     // 1 默认地址，0 否

    /** 拼接后的完整地址（省市区 + 详细） */
    public String getFullAddress() {
        return safe(province) + safe(city) + safe(district) + safe(detail);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isDefaultAddress() {
        return isDefault != null && isDefault == 1;
    }
}
