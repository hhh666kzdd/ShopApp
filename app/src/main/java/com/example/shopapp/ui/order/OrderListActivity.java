package com.example.shopapp.ui.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.common.PageResult;
import com.example.shopapp.common.Result;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.ui.base.LoadMoreListener;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.vo.OrderVO;

import java.util.concurrent.Callable;

/**
 * 我的订单列表（接口文档 8.4）：按状态筛选、分页加载；支付（8.6）、取消（8.7）、确认收货（8.8）。
 */
public class OrderListActivity extends BaseActivity implements OrderAdapter.Listener {

    private static final String EXTRA_STATUS = "status";
    private static final int PAGE_SIZE = 10;

    private OrderAdapter adapter;
    private View emptyView;

    private Integer status;   // null 表示全部
    private int page = 1;
    private boolean hasMore = true;
    private boolean loading = false;

    /**
     * 打开订单列表。
     *
     * @param status 初始筛选状态（OrderVO.STATUS_*），null 表示全部
     */
    public static void start(Context context, Integer status) {
        Intent intent = new Intent(context, OrderListActivity.class);
        if (status != null) intent.putExtra(EXTRA_STATUS, status);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        setupToolbar("我的订单", true);

        int s = getIntent().getIntExtra(EXTRA_STATUS, -1);
        status = s >= 0 ? s : null;

        emptyView = findViewById(R.id.empty_view);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(this);
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new LoadMoreListener(() -> {
            if (hasMore && !loading) load(false);
        }));

        RadioGroup rgStatus = findViewById(R.id.rg_status);
        rgStatus.check(radioIdOf(status));
        rgStatus.setOnCheckedChangeListener((group, checkedId) -> {
            status = statusOf(checkedId);
            load(true);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 首次进入及从详情页返回时都重新加载，保证状态最新
        load(true);
    }

    private int radioIdOf(Integer status) {
        if (status == null) return R.id.rb_all;
        switch (status) {
            case OrderVO.STATUS_UNPAID:
                return R.id.rb_unpaid;
            case OrderVO.STATUS_PAID:
                return R.id.rb_paid;
            case OrderVO.STATUS_SHIPPED:
                return R.id.rb_shipped;
            case OrderVO.STATUS_FINISHED:
                return R.id.rb_finished;
            case OrderVO.STATUS_CANCELED:
                return R.id.rb_canceled;
            default:
                return R.id.rb_all;
        }
    }

    private Integer statusOf(int checkedId) {
        if (checkedId == R.id.rb_unpaid) return OrderVO.STATUS_UNPAID;
        if (checkedId == R.id.rb_paid) return OrderVO.STATUS_PAID;
        if (checkedId == R.id.rb_shipped) return OrderVO.STATUS_SHIPPED;
        if (checkedId == R.id.rb_finished) return OrderVO.STATUS_FINISHED;
        if (checkedId == R.id.rb_canceled) return OrderVO.STATUS_CANCELED;
        return null;
    }

    private void load(boolean refresh) {
        if (refresh) {
            page = 1;
            hasMore = true;
        }
        loading = true;
        final int requestPage = page;
        final Integer requestStatus = status;
        ApiExecutor.run(() -> DaoFactory.order().listOrders(userId(), requestStatus, requestPage, PAGE_SIZE),
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

    @Override
    public void onClick(OrderVO order) {
        OrderDetailActivity.start(this, order.getId());
    }

    @Override
    public void onPay(OrderVO order) {
        confirmThen("确定支付该订单？（模拟支付）", "支付", "支付中…",
                () -> DaoFactory.order().payOrder(userId(), order.getId()), "支付成功");
    }

    @Override
    public void onCancel(OrderVO order) {
        confirmThen("确定取消该订单？", "取消订单", "处理中…",
                () -> DaoFactory.order().cancelOrder(userId(), order.getId()), "订单已取消");
    }

    @Override
    public void onConfirm(OrderVO order) {
        confirmThen("确认已收到商品？", "确认收货", "处理中…",
                () -> DaoFactory.order().confirmReceipt(userId(), order.getId()), "已确认收货");
    }

    /** 弹确认框，确认后执行订单操作并刷新列表 */
    private void confirmThen(String message, String positive, String loadingText,
                             Callable<Result<Void>> task, String successText) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNegativeButton("取消", null)
                .setPositiveButton(positive, (d, w) -> {
                    showLoading(loadingText);
                    ApiExecutor.run(task, new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            hideLoading();
                            toast(successText);
                            load(true);
                        }

                        @Override
                        public void onError(int code, String message) {
                            hideLoading();
                            toast(message);
                            load(true);
                        }
                    });
                })
                .show();
    }
}
