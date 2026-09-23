package com.example.shopapp.ui.user;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.ValidateUtil;
import com.example.shopapp.vo.UserVO;

/**
 * 注册页（接口文档 2.1）。
 */
public class RegisterActivity extends BaseActivity {

    private EditText etUsername, etPassword, etConfirm, etNickname, etPhone;

    @Override
    protected boolean requireLogin() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupToolbar("注册账号", true);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirm = findViewById(R.id.et_confirm);
        etNickname = findViewById(R.id.et_nickname);
        etPhone = findViewById(R.id.et_phone);
        findViewById(R.id.btn_register).setOnClickListener(v -> register());
    }

    private void register() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();
        String nickname = etNickname.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // 客户端先做一遍格式校验，DAO 内部会再次校验
        if (!ValidateUtil.isUsername(username)) {
            toast("用户名需为 4~20 位字母或数字");
            return;
        }
        if (!ValidateUtil.isPassword(password)) {
            toast("密码需为 6~20 位");
            return;
        }
        if (!password.equals(confirm)) {
            toast("两次输入的密码不一致");
            return;
        }
        if (!ValidateUtil.isPhoneOrEmpty(phone)) {
            toast("手机号格式不正确");
            return;
        }
        showLoading("注册中…");
        ApiExecutor.run(() -> DaoFactory.user().register(username, password, nickname, phone), new ApiCallback<UserVO>() {
            @Override
            public void onSuccess(UserVO data) {
                hideLoading();
                toast("注册成功，请登录");
                finish();
            }

            @Override
            public void onError(int code, String message) {
                hideLoading();
                toast(message);
            }
        });
    }
}
