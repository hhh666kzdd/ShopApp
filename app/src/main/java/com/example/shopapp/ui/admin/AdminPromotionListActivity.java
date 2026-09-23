package com.example.shopapp.ui.admin;

import android.os.Bundle;
import android.view.View;

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
import com.example.shopapp.vo.PromotionVO;

/**
 * 后台限时特价活动列表【管理员】（接口文档 6.2.3）：分页展示，支持新增、编辑、删除。
 */
public class AdminPromotionListActivity extends BaseActivity implements AdminPromotionAdapter.Listener {

    private static final int PAGE_SIZE = 10;

    private AdminPromotionAdapter adapter;
    private View emptyView;

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
        setContentView(R.layout.activity_admin_promotion_list);
        setupToolbar("特价活动管理", true);

        emptyView = findViewById(R.id.empty_view);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminPromotionAdapter(this);
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new LoadMoreListener(() -> {
            if (hasMore && !loading) load(false);
        }));

        findViewById(R.id.btn_add).setOnClickListener(v -> AdminPromotionEditActivity.start(this, null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) load(true);
    }

    private void load(boolean refresh) {
        if (refresh) {
            page = 1;
            hasMore = true;
        }
        loading = true;
        final int requestPage = page;
        ApiExecutor.run(() -> DaoFactory.promotion().listAllPromotions(userId(), requestPage, PAGE_SIZE),
                new ApiCallback<PageResult<PromotionVO>>() {
                    @Override
                    public void onSuccess(PageResult<PromotionVO> data) {
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
    public void onEdit(PromotionVO promotion) {
        AdminPromotionEditActivity.start(this, promotion);
    }

    @Override
    public void onDelete(PromotionVO promotion) {
        new AlertDialog.Builder(this)
                .setMessage("确定删除活动「" + promotion.getTitle() + "」？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (d, w) -> {
                    showLoading();
                    ApiExecutor.run(() -> DaoFactory.promotion().deletePromotion(userId(), promotion.getId()), new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            hideLoading();
                            toast("已删除");
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
}
