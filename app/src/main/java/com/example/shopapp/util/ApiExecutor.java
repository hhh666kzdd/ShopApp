package com.example.shopapp.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.shopapp.common.Result;
import com.example.shopapp.common.ResultCode;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 接口执行器（接口文档 1.3）：在线程池中执行 DAO 方法，结果回调到主线程。
 * <pre>
 * ApiExecutor.run(() -> userDao.login(username, password), new ApiCallback&lt;LoginVO&gt;() {
 *     public void onSuccess(LoginVO data) { ... }
 *     public void onError(int code, String message) { ... }
 * });
 * </pre>
 */
public class ApiExecutor {

    private static final String TAG = "ApiExecutor";

    /** 固定大小线程池，避免同时打开过多数据库连接 */
    private static final ExecutorService POOL = Executors.newFixedThreadPool(4);

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private ApiExecutor() {
    }

    /**
     * 执行一个返回 Result 的任务。
     *
     * @param task     DAO 调用
     * @param callback 主线程回调
     */
    public static <T> void run(Callable<Result<T>> task, ApiCallback<T> callback) {
        POOL.execute(() -> {
            Result<T> result;
            try {
                result = task.call();
                if (result == null) {
                    result = Result.fail(ResultCode.SERVER_ERROR, "接口未返回数据");
                }
            } catch (Exception e) {
                Log.e(TAG, "接口执行异常", e);
                result = Result.fail(ResultCode.SERVER_ERROR, "系统异常：" + e.getMessage());
            }
            final Result<T> r = result;
            MAIN_HANDLER.post(() -> {
                if (callback == null) return;
                if (r.isSuccess()) {
                    callback.onSuccess(r.getData());
                } else {
                    callback.onError(r.getCode(), r.getMessage());
                }
            });
        });
    }

    /**
     * 执行一个直接返回业务值的任务（如 getEffectivePrice、calcDiscount 这类非 Result 方法）。
     */
    public static <T> void runValue(Callable<T> task, ApiCallback<T> callback) {
        run(() -> Result.ok(task.call()), callback);
    }

    /** 在主线程执行 */
    public static void runOnMain(Runnable runnable) {
        MAIN_HANDLER.post(runnable);
    }
}
