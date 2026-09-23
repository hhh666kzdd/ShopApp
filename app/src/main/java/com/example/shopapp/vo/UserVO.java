package com.example.shopapp.vo;

import java.io.Serializable;

/**
 * 用户信息 VO（接口文档 2.7），不包含密码。
 */
public class UserVO implements Serializable {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer role;      // 0 普通用户，1 管理员
    private Integer status;    // 1 正常，0 禁用
    private String createTime; // 注册时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
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

    public boolean isAdmin() {
        return role != null && role == 1;
    }

    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
