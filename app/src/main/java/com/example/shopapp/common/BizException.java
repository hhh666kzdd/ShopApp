package com.example.shopapp.common;

/**
 * 业务异常：DAO 内部校验失败时抛出，携带状态码，
 * 由 BaseDao 统一捕获并转换为 {@link Result#fail(int, String)}。
 * 在事务中抛出该异常会触发回滚。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code) {
        super(ResultCode.getMessage(code));
        this.code = code;
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
