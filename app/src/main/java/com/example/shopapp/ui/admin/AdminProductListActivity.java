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
import com.example.shopapp.vo.ProductVO;

/**
 * 后台商品列表【管理员】（接口文档 4.8）：搜索、分页、编辑、上下架（4.6）、删除（4.7）、新增。
 */
public class AdminProductListActivity extends BaseActivity implements AdminProductAdapter.Listener {

    private static final int PAGE_SIZE = 10;

    private EditText etSearch;
    private AdminProductAdapter adapter;
    private View emptyView;

    private String keyword;
    private int page = 1;
    private boolean hasMore = true;
    private boolean loading = false;
    private boolean created = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isAdmin()) {
            toast("无权限");
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_product_list);
        setupToolbar("商品管理", true);

        etSearch = findViewById(R.id.et_search);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });
        findViewById(R.id.tv_search).setOnClickListener(v -> doSearch());
        findViewById(R.id.btn_add).setOnClickListener(v -> AdminProductEditActivity.start(this, null));

        emptyView = findViewById(R.id.empty_view);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminProductAdapter(this);
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new LoadMoreListener(() -> {
            if (hasMore && !loading) load(false);
        }));
        created = true;
    }

    /** 新增 / 编辑返回后刷新第一页 */
    @Override
    protected void onResume() {
        super.onResume();
        if (created) load(true);
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
        ApiExecutor.run(() -> DaoFactory.product().listAllProducts(userId(), keyword, requestPage, PAGE_SIZE),
                new ApiCallback<PageResult<ProductVO>>() {
                    @Override
                    public void onSuccess(PageResult<ProductVO> data) {
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
    public void onEdit(ProductVO product) {
        AdminProductEditActivity.start(this, product);
    }

    @Override
    public void onToggleStatus(ProductVO product) {
        int newStatus = product.isOnSale() ? 0 : 1;
        showLoading();
        ApiExecutor.run(() -> DaoFactory.product().updateProductStatus(userId(), product.getId(), newStatus), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                hideLoading();
                toast(newStatus == 1 ? "已上架" : "已下架");
                load(true);
            }

            @Override
            public void onError(int code, String message) {
                hideLoading();
                toast(message);
            }
        });
    }

    @Override
    public void onDelete(ProductVO product) {
        new AlertDialog.Builder(this)
                .setMessage("确定删除「" + product.getName() + "」？已产生订单的商品建议下架而非删除。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (d, w) -> {
                    showLoading();
                    ApiExecutor.run(() -> DaoFactory.product().deleteProduct(userId(), product.getId()), new ApiCallback<Void>() {
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
