package com.example.shopapp.util;

import com.example.shopapp.common.ResultCode;

/**
 * 接口回调（接口文档 1.3），两个方法均在主线程回调。
 *
 * @param <T> 业务数据类型
 */
public interface ApiCallback<T> {

    /** 成功，data 为 Result.data（无数据接口为 null） */
    void onSuccess(T data);

    /** 失败，默认弹出错误提示，调用方可覆盖 */
    default void onError(int code, String message) {
        ToastUtil.show(message == null ? ResultCode.getMessage(code) : message);
    }
}
