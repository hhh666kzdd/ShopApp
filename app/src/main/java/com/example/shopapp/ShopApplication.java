package com.example.shopapp;

import android.app.Application;
import android.content.Context;

import com.example.shopapp.util.SessionManager;

/**
 * 应用入口，负责初始化全局单例（登录会话等），并提供全局 Context。
 */
public class ShopApplication extends Application {

    private static ShopApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 会话管理器依赖 SharedPreferences，需要在最早的时机初始化
        SessionManager.init(this);
    }

    /** 获取全局 Application Context，供工具类（Toast、SharedPreferences）使用 */
    public static Context getContext() {
        return instance.getApplicationContext();
    }
}
