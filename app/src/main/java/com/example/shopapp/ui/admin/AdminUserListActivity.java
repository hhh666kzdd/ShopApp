package com.example.shopapp.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.common.PageResult;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.ui.base.LoadMoreListener;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.vo.UserVO;

/**
 * 后台用户管理【管理员】（接口文档 9.1 / 9.2 / 9.3）：搜索、分页、启用 / 禁用、重置密码。
 */
public class AdminUserListActivity extends BaseActivity implements AdminUserAdapter.Listener {

    private static final int PAGE_SIZE = 10;

    private EditText etSearch;
    private AdminUserAdapter adapter;
    private View emptyView;

    private String keyword;
    private int page = 1;
    private boolean hasMore = true;
    private boolean loading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isAdmin()) {
            toast("无权限");
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_user_list);
        setupToolbar("用户管理", true);

        etSearch = findViewById(R.id.et_search);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });
        findViewById(R.id.tv_search).setOnClickListener(v -> doSearch());

        emptyView = findViewById(R.id.empty_view);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(this);
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new LoadMoreListener(() -> {
            if (hasMore && !loading) load(false);
        }));

        load(true);
    }

    private void doSearch() {
        keyword = etSearch.getText().toString().trim();
        load(true);
    }

    private void load(boolean refresh) {
        if (refresh) {
            page = 1;
            hasMore = true;
        }
        loading = true;
        final int requestPage = page;
        ApiExecutor.run(() -> DaoFactory.admin().listUsers(userId(), keyword, requestPage, PAGE_SIZE),
                new ApiCallback<PageResult<UserVO>>() {
                    @Override
                    public void onSuccess(PageResult<UserVO> data) {
                        loading = false;
                        if (requestPage == 1) {
                            adapter.setData(data.getRecords());
                        } else {
                            adapter.addData(data.getRecords());
                        }
                        hasMore = data.hasMore();
                        page = requestPage + 1;
                        toggleEmpty(emptyView, adapter.isEmpty());
                    }

                    @Override
                    public void onError(int code, String message) {
                        loading = false;
                        toast(message);
                    }
                });
    }

    @Override
    public void onToggleStatus(UserVO user) {
        int newStatus = user.isEnabled() ? 0 : 1;
        String action = newStatus == 1 ? "启用" : "禁用";
        new AlertDialog.Builder(this)
                .setMessage("确定" + action + "用户「" + user.getUsername() + "」？")
                .setNegativeButton("取消", null)
                .setPositiveButton(action, (d, w) -> {
                    showLoading();
                    ApiExecutor.run(() -> DaoFactory.admin().updateUserStatus(userId(), user.getId(), newStatus), new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            hideLoading();
                            toast("已" + action);
                            load(true);
                        }

                        @Override
                        public void onError(int code, String message) {
                            hideLoading();
                            toast(message);
                        }
                    });
                })
                .show();
    }

    @Override
    public void onResetPassword(UserVO user) {
        new AlertDialog.Builder(this)
                .setMessage("确定将用户「" + user.getUsername() + "」的密码重置为 123456？")
                .setNegativeButton("取消", null)
                .setPositiveButton("重置", (d, w) -> {
                    showLoading();
                    ApiExecutor.run(() -> DaoFactory.admin().resetPassword(userId(), user.getId()), new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            hideLoading();
                            toast("已重置为 123456");
                        }

                        @Override
                        public void onError(int code, String message) {
                            hideLoading();
                            toast(message);
                        }
                    });
                })
                .show();
    }
}
