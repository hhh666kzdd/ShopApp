package com.example.shopapp.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.ValidateUtil;

/**
 * 修改密码（接口文档 2.5），成功后需重新登录。
 */
public class ChangePasswordActivity extends BaseActivity {

    private EditText etOld, etNew, etConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        setupToolbar("修改密码", true);
        etOld = findViewById(R.id.et_old_password);
        etNew = findViewById(R.id.et_new_password);
        etConfirm = findViewById(R.id.et_confirm_password);
        findViewById(R.id.btn_submit).setOnClickListener(v -> submit());
    }

    private void submit() {
        String oldPwd = etOld.getText().toString();
        String newPwd = etNew.getText().toString();
        String confirm = etConfirm.getText().toString();
        if (oldPwd.isEmpty()) {
            toast("请输入原密码");
            return;
        }
        if (!ValidateUtil.isPassword(newPwd)) {
            toast("新密码需为 6~20 位");
            return;
        }
        if (!newPwd.equals(confirm)) {
            toast("两次输入的新密码不一致");
            return;
        }
        showLoading();
        ApiExecutor.run(() -> DaoFactory.user().updatePassword(userId(), oldPwd, newPwd), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                hideLoading();
                toast("密码已修改，请重新登录");
                DaoFactory.user().logout();
                Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
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
