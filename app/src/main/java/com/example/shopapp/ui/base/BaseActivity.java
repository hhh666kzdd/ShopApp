package com.example.shopapp.ui.base;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.shopapp.R;
import com.example.shopapp.ui.user.LoginActivity;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.util.ToastUtil;

/**
 * Activity 基类：统一 Toolbar、加载框、Toast 与登录态处理。
 */
public abstract class BaseActivity extends AppCompatActivity {

    private Dialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 需要登录的页面在未登录时跳转到登录页
        if (requireLogin() && !SessionManager.isLogin()) {
            toast("请先登录");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    /** 子类可覆盖：该页面是否需要登录，默认需要 */
    protected boolean requireLogin() {
        return true;
    }

    /**
     * 初始化布局中 id 为 toolbar 的 MaterialToolbar。
     *
     * @param title    标题
     * @param showBack 是否显示返回箭头
     */
    protected void setupToolbar(String title, boolean showBack) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) return;
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(showBack);
        }
        if (showBack) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    /** 当前登录用户 ID */
    protected Long userId() {
        return SessionManager.getUserId();
    }

    protected void toast(String message) {
        ToastUtil.show(message);
    }

    /** 显示不可取消的加载框 */
    protected void showLoading(String message) {
        if (isFinishing()) return;
        if (loadingDialog == null) {
            loadingDialog = new Dialog(this);
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setContentView(R.layout.dialog_loading);
            loadingDialog.setCancelable(false);
            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
        TextView tv = loadingDialog.findViewById(R.id.tv_loading_text);
        if (tv != null) tv.setText(message == null ? getString(R.string.loading) : message);
        if (!loadingDialog.isShowing()) loadingDialog.show();
    }

    protected void showLoading() {
        showLoading(null);
    }

    protected void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /** 根据列表是否为空切换空视图 */
    protected void toggleEmpty(View emptyView, boolean empty) {
        if (emptyView != null) emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        hideLoading();
        loadingDialog = null;
        super.onDestroy();
    }
}
