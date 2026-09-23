package com.example.shopapp.ui.user;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.ui.base.ImageLoader;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.util.ValidateUtil;
import com.example.shopapp.vo.UserVO;

/**
 * 个人资料（接口文档 2.3 / 2.4）。
 */
public class ProfileEditActivity extends BaseActivity {

    private ImageView ivAvatar;
    private TextView tvUsername;
    private EditText etNickname, etPhone, etAvatar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        setupToolbar("个人资料", true);

        ivAvatar = findViewById(R.id.iv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        etNickname = findViewById(R.id.et_nickname);
        etPhone = findViewById(R.id.et_phone);
        etAvatar = findViewById(R.id.et_avatar);
        findViewById(R.id.btn_save).setOnClickListener(v -> save());

        loadInfo();
    }

    private void loadInfo() {
        showLoading();
        ApiExecutor.run(() -> DaoFactory.user().getUserInfo(userId()), new ApiCallback<UserVO>() {
            @Override
            public void onSuccess(UserVO data) {
                hideLoading();
                tvUsername.setText(data.getUsername());
                etNickname.setText(data.getNickname());
                etPhone.setText(data.getPhone());
                etAvatar.setText(data.getAvatar());
                ImageLoader.loadCircle(ProfileEditActivity.this, data.getAvatar(), ivAvatar);
            }

            @Override
            public void onError(int code, String message) {
                hideLoading();
                toast(message);
            }
        });
    }

    private void save() {
        String nickname = etNickname.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String avatar = etAvatar.getText().toString().trim();
        if (nickname.isEmpty()) {
            toast("昵称不能为空");
            return;
        }
        if (!ValidateUtil.isPhoneOrEmpty(phone)) {
            toast("手机号格式不正确");
            return;
        }
        showLoading("保存中…");
        ApiExecutor.run(() -> DaoFactory.user().updateUserInfo(userId(), nickname, phone, avatar), new ApiCallback<UserVO>() {
            @Override
            public void onSuccess(UserVO data) {
                hideLoading();
                // 同步本地会话，「我的」页面即时更新
                SessionManager.updateProfile(data.getNickname(), data.getAvatar());
                toast("保存成功");
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
