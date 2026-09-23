package com.example.shopapp.ui.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.common.Result;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.OrderVO;

import java.util.concurrent.Callable;

/**
 * 订单详情（接口文档 8.5）：展示收货信息、商品明细、金额与时间；
 * 待付款可支付（8.6）/ 取消（8.7），待收货可确认收货（8.8）。
 */
public class OrderDetailActivity extends BaseActivity {

    private static final String EXTRA_ORDER_ID = "orderId";

    private TextView tvStatus, tvReceiver, tvAddress, tvTotalAmount, tvDiscountAmount, tvPayAmount,
            tvOrderNo, tvCreateTime, tvPayTime, tvShipTime, tvFinishTime, tvRemark;
    private View layoutActions;
    private Button btnCancel, btnPay, btnConfirm;
    private OrderItemAdapter itemAdapter;

    private Long orderId;

    public static void start(Context context, Long orderId) {
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra(EXTRA_ORDER_ID, orderId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        setupToolbar("订单详情", true);
        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);

        tvStatus = findViewById(R.id.tv_status);
        tvReceiver = findViewById(R.id.tv_receiver);
        tvAddress = findViewById(R.id.tv_address);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvDiscountAmount = findViewById(R.id.tv_discount_amount);
        tvPayAmount = findViewById(R.id.tv_pay_amount);
        tvOrderNo = findViewById(R.id.tv_order_no);
        tvCreateTime = findViewById(R.id.tv_create_time);
        tvPayTime = findViewById(R.id.tv_pay_time);
        tvShipTime = findViewById(R.id.tv_ship_time);
        tvFinishTime = findViewById(R.id.tv_finish_time);
        tvRemark = findViewById(R.id.tv_remark);
        layoutActions = findViewById(R.id.layout_actions);
        btnCancel = findViewById(R.id.btn_cancel);
        btnPay = findViewById(R.id.btn_pay);
        btnConfirm = findViewById(R.id.btn_confirm);

        RecyclerView rvItems = findViewById(R.id.rv_items);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new OrderItemAdapter();
        rvItems.setAdapter(itemAdapter);

        btnPay.setOnClickListener(v -> confirmThen("确定支付该订单？（模拟支付）", "支付", "支付中…",
                () -> DaoFactory.order().payOrder(userId(), orderId), "支付成功"));
        btnCancel.setOnClickListener(v -> confirmThen("确定取消该订单？", "取消订单", "处理中…",
                () -> DaoFactory.order().cancelOrder(userId(), orderId), "订单已取消"));
        btnConfirm.setOnClickListener(v -> confirmThen("确认已收到商品？", "确认收货", "处理中…",
                () -> DaoFactory.order().confirmReceipt(userId(), orderId), "已确认收货"));

        loadDetail();
    }

    private void loadDetail() {
        showLoading();
        ApiExecutor.run(() -> DaoFactory.order().getOrderDetail(userId(), orderId), new ApiCallback<OrderVO>() {
            @Override
            public void onSuccess(OrderVO data) {
                hideLoading();
                render(data);
            }

            @Override
            public void onError(int code, String message) {
                hideLoading();
                toast(message);
                finish();
            }
        });
    }

    private void render(OrderVO order) {
        tvStatus.setText(order.getStatusText());
        tvReceiver.setText(safe(order.getReceiverName()) + "  " + safe(order.getReceiverPhone()));
        tvAddress.setText(safe(order.getReceiverAddress()));
        itemAdapter.setData(order.getItems());

        tvTotalAmount.setText(MoneyUtil.format(order.getTotalAmount()));
        tvDiscountAmount.setText("-" + MoneyUtil.format(order.getDiscountAmount()));
        tvPayAmount.setText(MoneyUtil.format(order.getPayAmount()));

        tvOrderNo.setText("订单号：" + safe(order.getOrderNo()));
        tvCreateTime.setText("下单时间：" + safe(order.getCreateTime()));
        setOptional(tvPayTime, "支付时间：", order.getPayTime());
        setOptional(tvShipTime, "发货时间：", order.getShipTime());
        setOptional(tvFinishTime, "完成时间：", order.getFinishTime());
        setOptional(tvRemark, "备注：", order.getRemark());

        int status = order.getStatus() == null ? -1 : order.getStatus();
        boolean unpaid = status == OrderVO.STATUS_UNPAID;
        boolean shipped = status == OrderVO.STATUS_SHIPPED;
        btnCancel.setVisibility(unpaid ? View.VISIBLE : View.GONE);
        btnPay.setVisibility(unpaid ? View.VISIBLE : View.GONE);
        btnConfirm.setVisibility(shipped ? View.VISIBLE : View.GONE);
        layoutActions.setVisibility(unpaid || shipped ? View.VISIBLE : View.GONE);
    }

    /** 值为空时隐藏该行 */
    private void setOptional(TextView tv, String label, String value) {
        if (value == null || value.trim().isEmpty()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(label + value);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    /** 弹确认框，确认后执行订单操作并重新加载详情 */
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
                            loadDetail();
                        }

                        @Override
                        public void onError(int code, String message) {
                            hideLoading();
                            toast(message);
                            loadDetail();
                        }
                    });
                })
                .show();
    }
}
