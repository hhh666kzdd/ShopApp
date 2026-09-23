package com.example.shopapp.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioGroup;

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
import com.example.shopapp.vo.OrderVO;

/**
 * 后台订单列表【管理员】（接口文档 8.9 / 8.10）：按状态、订单号筛选，待发货订单可发货。
 */
public class AdminOrderListActivity extends BaseActivity implements AdminOrderAdapter.Listener {

    private static final int PAGE_SIZE = 10;

    private EditText etSearch;
    private AdminOrderAdapter adapter;
    private View emptyView;

    /** null 表示全部状态 */
    private Integer status = null;
    private String orderNo;
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
        setContentView(R.layout.activity_admin_order_list);
        setupToolbar("订单管理", true);

        etSearch = findViewById(R.id.et_search);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });
        findViewById(R.id.tv_search).setOnClickListener(v -> doSearch());

        RadioGroup rgStatus = findViewById(R.id.rg_status);
        rgStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_unpaid) {
                status = OrderVO.STATUS_UNPAID;
            } else if (checkedId == R.id.rb_paid) {
                status = OrderVO.STATUS_PAID;
            } else if (checkedId == R.id.rb_shipped) {
                status = OrderVO.STATUS_SHIPPED;
            } else if (checkedId == R.id.rb_finished) {
                status = OrderVO.STATUS_FINISHED;
            } else if (checkedId == R.id.rb_canceled) {
                status = OrderVO.STATUS_CANCELED;
            } else {
                status = null;
            }
            load(true);
        });

        emptyView = findViewById(R.id.empty_view);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(this);
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new LoadMoreListener(() -> {
            if (hasMore && !loading) load(false);
        }));

        load(true);
    }

    private void doSearch() {
        String s = etSearch.getText().toString().trim();
        orderNo = s.isEmpty() ? null : s;
        load(true);
    }

    private void load(boolean refresh) {
        if (refresh) {
            page = 1;
            hasMore = true;
        }
        loading = true;
        final int requestPage = page;
        final Integer reqStatus = status;
        final String reqOrderNo = orderNo;
        ApiExecutor.run(() -> DaoFactory.order().listAllOrders(userId(), reqStatus, reqOrderNo, requestPage, PAGE_SIZE),
                new ApiCallback<PageResult<OrderVO>>() {
                    @Override
                    public void onSuccess(PageResult<OrderVO> data) {
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

    /** 发货（接口文档 8.10）：仅待发货订单可操作 */
    @Override
    public void onShip(OrderVO order) {
        new AlertDialog.Builder(this)
                .setMessage("确认订单 " + order.getOrderNo() + " 已发货？")
                .setNegativeButton("取消", null)
                .setPositiveButton("发货", (d, w) -> {
                    showLoading("处理中…");
                    ApiExecutor.run(() -> DaoFactory.order().shipOrder(userId(), order.getId()), new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            hideLoading();
                            toast("已发货");
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
