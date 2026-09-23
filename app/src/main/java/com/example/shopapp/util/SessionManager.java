package com.example.shopapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.shopapp.vo.LoginVO;

/**
 * 登录会话管理（接口文档 1.3）：登录成功后把 userId、role 等信息保存到 SharedPreferences，
 * 后续接口中的 userId 参数均取自 {@link #getUserId()}。
 */
public class SessionManager {

    private static final String PREF_NAME = "session";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_ROLE = "role";

    /** 管理员角色值 */
    public static final int ROLE_ADMIN = 1;

    private static SharedPreferences prefs;

    private SessionManager() {
    }

    /** 在 Application.onCreate 中初始化 */
    public static void init(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** 登录成功后保存会话 */
    public static void saveLogin(LoginVO vo) {
        prefs.edit()
                .putLong(KEY_USER_ID, vo.getUserId())
                .putString(KEY_USERNAME, vo.getUsername())
                .putString(KEY_NICKNAME, vo.getNickname())
                .putString(KEY_AVATAR, vo.getAvatar())
                .putInt(KEY_ROLE, vo.getRole() == null ? 0 : vo.getRole())
                .apply();
    }

    /** 修改资料后同步更新本地缓存的昵称、头像 */
    public static void updateProfile(String nickname, String avatar) {
        prefs.edit().putString(KEY_NICKNAME, nickname).putString(KEY_AVATAR, avatar).apply();
    }

    public static boolean isLogin() {
        return prefs.getLong(KEY_USER_ID, 0) > 0;
    }

    /** 当前用户 ID，未登录返回 null */
    public static Long getUserId() {
        long id = prefs.getLong(KEY_USER_ID, 0);
        return id > 0 ? id : null;
    }

    public static String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public static String getNickname() {
        return prefs.getString(KEY_NICKNAME, "");
    }

    public static String getAvatar() {
        return prefs.getString(KEY_AVATAR, null);
    }

    public static int getRole() {
        return prefs.getInt(KEY_ROLE, 0);
    }

    public static boolean isAdmin() {
        return getRole() == ROLE_ADMIN;
    }

    /** 退出登录（接口文档 2.6）：清除本地会话，不访问数据库 */
    public static void logout() {
        prefs.edit().clear().apply();
    }
}
