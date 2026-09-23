package com.example.shopapp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 工具：密码统一 MD5 后存储 / 比对，32 位小写（接口文档 1.7）。
 */
public class MD5Util {

    private MD5Util() {
    }

    public static String md5(String text) {
        if (text == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 算法不可用", e);
        }
    }
}
