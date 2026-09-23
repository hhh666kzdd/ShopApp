package com.example.shopapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.shopapp.ShopApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接工具（接口文档 11.2）。
 * <p>
 * Android 端通过 JDBC（mysql-connector-java 5.1.49）直连 MySQL。
 * 连接参数（主机、端口、账号）保存在 SharedPreferences 中，可在登录页「服务器设置」里修改，
 * 便于在模拟器（10.0.2.2 指向本机）和真机（电脑局域网 IP）之间切换。
 * <p>
 * 注意：所有数据库操作必须在子线程执行，统一通过 {@link ApiExecutor} 调用。
 */
public class DBUtil {

    private static final String PREF_NAME = "db_config";

    /** 默认主机：Android 模拟器访问宿主机的固定地址 */
    public static final String DEFAULT_HOST = "10.0.2.2";
    public static final int DEFAULT_PORT = 3306;
    public static final String DEFAULT_DB = "shop_db";
    /** 默认账号（MySQL 8 需使用 mysql_native_password，见 sql/create_user.sql） */
    public static final String DEFAULT_USER = "shop";
    public static final String DEFAULT_PASSWORD = "123456";

    private static volatile boolean driverLoaded = false;

    private DBUtil() {
    }

    /** 加载驱动，只执行一次 */
    private static void loadDriver() throws SQLException {
        if (!driverLoaded) {
            synchronized (DBUtil.class) {
                if (!driverLoaded) {
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        driverLoaded = true;
                    } catch (ClassNotFoundException e) {
                        throw new SQLException("MySQL 驱动加载失败", e);
                    }
                }
            }
        }
    }

    private static SharedPreferences prefs() {
        return ShopApplication.getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getHost() {
        return prefs().getString("host", DEFAULT_HOST);
    }

    public static int getPort() {
        return prefs().getInt("port", DEFAULT_PORT);
    }

    public static String getUser() {
        return prefs().getString("user", DEFAULT_USER);
    }

    public static String getPassword() {
        return prefs().getString("password", DEFAULT_PASSWORD);
    }

    /** 保存服务器配置（登录页「服务器设置」调用） */
    public static void saveConfig(String host, int port, String user, String password) {
        prefs().edit()
                .putString("host", host)
                .putInt("port", port)
                .putString("user", user)
                .putString("password", password)
                .apply();
    }

    /** 拼接 JDBC URL */
    public static String getUrl() {
        return "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + DEFAULT_DB
                + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
                + "&serverTimezone=Asia/Shanghai&connectTimeout=5000&socketTimeout=15000";
    }

    /**
     * 获取数据库连接，调用方负责关闭（建议使用 try-with-resources）。
     */
    public static Connection getConnection() throws SQLException {
        loadDriver();
        return DriverManager.getConnection(getUrl(), getUser(), getPassword());
    }

    /** 静默关闭资源（ResultSet / Statement / Connection） */
    public static void close(AutoCloseable... resources) {
        if (resources == null) return;
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
