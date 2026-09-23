package com.example.shopapp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具：金额统一 BigDecimal，保留 2 位小数（接口文档 1.7）。
 */
public class MoneyUtil {

    private MoneyUtil() {
    }

    /** 保留两位小数，四舍五入；null 视为 0 */
    public static BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    /** 格式化为「¥12.00」 */
    public static String format(BigDecimal value) {
        return "¥" + scale(value).toPlainString();
    }

    /** 格式化为不带符号的「12.00」 */
    public static String plain(BigDecimal value) {
        return scale(value).toPlainString();
    }

    /** 字符串安全转 BigDecimal，非法返回 null */
    public static BigDecimal parse(String text) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty()) return null;
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 判断金额是否大于 0 */
    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }
}
