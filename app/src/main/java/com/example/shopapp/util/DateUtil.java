package com.example.shopapp.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具：时间统一为字符串 yyyy-MM-dd HH:mm:ss（接口文档 1.7）。
 */
public class DateUtil {

    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    private DateUtil() {
    }

    private static SimpleDateFormat formatter() {
        // SimpleDateFormat 非线程安全，每次新建（调用频率不高）
        return new SimpleDateFormat(PATTERN, Locale.CHINA);
    }

    /** 数据库 Timestamp 转字符串，null 返回 null */
    public static String format(Timestamp ts) {
        return ts == null ? null : formatter().format(new Date(ts.getTime()));
    }

    public static String format(Date date) {
        return date == null ? null : formatter().format(date);
    }

    /** 字符串转 Timestamp，格式不正确返回 null */
    public static Timestamp parse(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            Date d = formatter().parse(text.trim());
            return d == null ? null : new Timestamp(d.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    /** 当前时间字符串 */
    public static String now() {
        return formatter().format(new Date());
    }

    /** 生成订单号：yyyyMMddHHmmss + 6 位随机数（接口文档 8.3） */
    public static String genOrderNo() {
        String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
        int random = (int) (Math.random() * 900000) + 100000;
        return time + random;
    }
}
