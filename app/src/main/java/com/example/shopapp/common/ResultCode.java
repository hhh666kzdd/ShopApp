package com.example.shopapp.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 状态码定义（接口文档 1.6）。
 */
public final class ResultCode {

    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;        // 参数错误
    public static final int UNAUTHORIZED = 401;       // 未登录
    public static final int FORBIDDEN = 403;          // 无权限
    public static final int NOT_FOUND = 404;          // 记录不存在
    public static final int SERVER_ERROR = 500;       // 数据库 / 系统异常

    public static final int USER_EXISTS = 1001;       // 用户名已存在
    public static final int LOGIN_FAILED = 1002;      // 用户名或密码错误
    public static final int USER_DISABLED = 1003;     // 账号已被禁用
    public static final int OLD_PASSWORD_WRONG = 1004;// 原密码错误

    public static final int PRODUCT_OFF = 2001;       // 商品不存在或已下架
    public static final int STOCK_NOT_ENOUGH = 2002;  // 商品库存不足

    public static final int COUPON_INVALID = 3001;    // 优惠券不存在 / 未开始 / 已过期 / 已领完
    public static final int COUPON_LIMIT = 3002;      // 领取数量已达上限
    public static final int COUPON_UNUSABLE = 3003;   // 优惠券不可用

    public static final int ORDER_NOT_FOUND = 4001;   // 订单不存在
    public static final int ORDER_STATUS_ERROR = 4002;// 当前订单状态不允许该操作

    public static final int ADDRESS_NOT_FOUND = 5001; // 收货地址不存在

    private static final Map<Integer, String> MESSAGES = new HashMap<>();

    static {
        MESSAGES.put(SUCCESS, "success");
        MESSAGES.put(BAD_REQUEST, "参数错误");
        MESSAGES.put(UNAUTHORIZED, "未登录");
        MESSAGES.put(FORBIDDEN, "无权限");
        MESSAGES.put(NOT_FOUND, "记录不存在");
        MESSAGES.put(SERVER_ERROR, "系统异常");
        MESSAGES.put(USER_EXISTS, "用户名已存在");
        MESSAGES.put(LOGIN_FAILED, "用户名或密码错误");
        MESSAGES.put(USER_DISABLED, "账号已被禁用");
        MESSAGES.put(OLD_PASSWORD_WRONG, "原密码错误");
        MESSAGES.put(PRODUCT_OFF, "商品不存在或已下架");
        MESSAGES.put(STOCK_NOT_ENOUGH, "商品库存不足");
        MESSAGES.put(COUPON_INVALID, "优惠券不存在或不在有效期内");
        MESSAGES.put(COUPON_LIMIT, "优惠券领取数量已达上限");
        MESSAGES.put(COUPON_UNUSABLE, "优惠券不可用");
        MESSAGES.put(ORDER_NOT_FOUND, "订单不存在");
        MESSAGES.put(ORDER_STATUS_ERROR, "当前订单状态不允许该操作");
        MESSAGES.put(ADDRESS_NOT_FOUND, "收货地址不存在");
    }

    private ResultCode() {
    }

    /** 获取状态码对应的默认提示，未知状态码返回「未知错误」 */
    public static String getMessage(int code) {
        String msg = MESSAGES.get(code);
        return msg == null ? "未知错误(" + code + ")" : msg;
    }
}
