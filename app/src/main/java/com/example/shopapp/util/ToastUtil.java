package com.example.shopapp.util;

import android.widget.Toast;

import com.example.shopapp.ShopApplication;

/**
 * Toast 工具：复用同一个 Toast 实例，避免连续提示时排队堆积。
 */
public class ToastUtil {

    private static Toast toast;

    private ToastUtil() {
    }

    public static void show(String message) {
        if (message == null || message.isEmpty()) return;
        ApiExecutor.runOnMain(() -> {
            if (toast != null) toast.cancel();
            toast = Toast.makeText(ShopApplication.getContext(), message, Toast.LENGTH_SHORT);
            toast.show();
        });
    }
}
