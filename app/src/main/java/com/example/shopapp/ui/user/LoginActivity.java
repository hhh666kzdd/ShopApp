package com.example.shopapp.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.admin.AdminMainActivity;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.ui.main.MainActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.DBUtil;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.vo.LoginVO;

/**
 * 登录页（应用入口）。已登录时直接进入商城；管理员登录后进入后台。
 */
public class LoginActivity extends BaseActivity {

    private EditText etUsername;
    private EditText etPassword;

    @Override
    protected boolean requireLogin() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 已登录直接跳转
        if (SessionManager.isLogin()) {
            goHome(SessionManager.isAdmin());
            return;
        }
        setContentView(R.layout.activity_login);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);

        findViewById(R.id.btn_login).setOnClickListener(v -> login());
        findViewById(R.id.tv_register).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        findViewById(R.id.tv_server).setOnClickListener(v -> showServerDialog());
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            toast("请输入用户名和密码");
            return;
        }
        showLoading("登录中…");
        ApiExecutor.run(() -> DaoFactory.user().login(username, password), new ApiCallback<LoginVO>() {
            @Override
            public void onSuccess(LoginVO data) {
                hideLoading();
                SessionManager.saveLogin(data);
                toast("欢迎，" + data.getNickname());
                goHome(data.getRole() != null && data.getRole() == SessionManager.ROLE_ADMIN);
            }

            @Override
            public void onError(int code, String message) {
                hideLoading();
                toast(message);
            }
        });
    }

    /** 管理员进入后台，普通用户进入商城 */
    private void goHome(boolean admin) {
        startActivity(new Intent(this, admin ? AdminMainActivity.class : MainActivity.class));
        finish();
    }

    /** 服务器连接设置：保存到 SharedPreferences，DBUtil 每次取最新配置 */
    private void showServerDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_server_config, null);
        EditText etHost = view.findViewById(R.id.et_host);
        EditText etPort = view.findViewById(R.id.et_port);
        EditText etUser = view.findViewById(R.id.et_db_user);
        EditText etPwd = view.findViewById(R.id.et_db_password);
        etHost.setText(DBUtil.getHost());
        etPort.setText(String.valueOf(DBUtil.getPort()));
        etUser.setText(DBUtil.getUser());
        etPwd.setText(DBUtil.getPassword());

        new AlertDialog.Builder(this)
                .setTitle("服务器设置")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (d, w) -> {
                    String host = etHost.getText().toString().trim();
                    int port;
                    try {
                        port = Integer.parseInt(etPort.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        port = DBUtil.DEFAULT_PORT;
                    }
                    if (TextUtils.isEmpty(host)) host = DBUtil.DEFAULT_HOST;
                    DBUtil.saveConfig(host, port, etUser.getText().toString().trim(), etPwd.getText().toString());
                    toast("已保存");
                })
                .show();
    }
}
