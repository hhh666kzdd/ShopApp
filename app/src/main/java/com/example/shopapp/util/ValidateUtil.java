package com.example.shopapp.util;

import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * 参数校验工具（用户名、密码、手机号等格式规则见接口文档 2.1）。
 */
public class ValidateUtil {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9]{4,20}$");
    private static final Pattern PHONE = Pattern.compile("^1\\d{10}$");

    private ValidateUtil() {
    }

    public static boolean isEmpty(String s) {
        return TextUtils.isEmpty(s) || s.trim().isEmpty();
    }

    /** 用户名：4~20 位字母数字 */
    public static boolean isUsername(String s) {
        return s != null && USERNAME.matcher(s).matches();
    }

    /** 密码：6~20 位 */
    public static boolean isPassword(String s) {
        return s != null && s.length() >= 6 && s.length() <= 20;
    }

    /** 手机号：11 位，1 开头；允许为空 */
    public static boolean isPhoneOrEmpty(String s) {
        return isEmpty(s) || PHONE.matcher(s.trim()).matches();
    }

    /** 手机号：必填 */
    public static boolean isPhone(String s) {
        return s != null && PHONE.matcher(s.trim()).matches();
    }

    /** 解析整数，失败返回 null */
    public static Integer parseInt(String s) {
        if (isEmpty(s)) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
