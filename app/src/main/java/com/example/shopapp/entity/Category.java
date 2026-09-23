package com.example.shopapp.entity;

import java.io.Serializable;

/**
 * 商品分类，对应 t_category 表（接口文档 3.1）。
 */
public class Category implements Serializable {

    private Long id;
    private String name;    // 分类名称
    private String icon;    // 图标 URL
    private Integer sort;   // 排序值，越小越靠前
    private Integer status; // 1 启用，0 停用

    public Category() {
    }

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /** Spinner 等控件直接显示分类名称 */
    @Override
    public String toString() {
        return name;
    }
}
