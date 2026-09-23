package com.example.shopapp.common;

/**
 * 统一返回结构（接口文档 1.4）。
 * code = 200 表示成功，其余为业务 / 系统错误码，见 {@link ResultCode}。
 *
 * @param <T> 业务数据类型，失败时 data 为 null
 */
public class Result<T> {

    private int code;        // 状态码，200 表示成功
    private String message;  // 提示信息
    private T data;          // 业务数据，失败时为 null

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功并携带数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS, "success", data);
    }

    /** 成功但无数据（如删除、修改操作） */
    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS, "success", null);
    }

    /** 失败，使用自定义提示 */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 失败，使用状态码对应的默认提示 */
    public static <T> Result<T> fail(int code) {
        return new Result<>(code, ResultCode.getMessage(code), null);
    }

    public boolean isSuccess() {
        return code == ResultCode.SUCCESS;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{code=" + code + ", message='" + message + "', data=" + data + '}';
    }
}
