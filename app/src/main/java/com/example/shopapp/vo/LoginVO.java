package com.example.shopapp.vo;

import java.io.Serializable;

/**
 * 登录返回 VO（接口文档 2.2）。
 */
public class LoginVO implements Serializable {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer role; // 0 普通用户，1 管理员

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
}
