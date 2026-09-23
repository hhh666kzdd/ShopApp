package com.example.shopapp.ui.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.common.ResultCode;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.entity.Address;
import com.example.shopapp.req.CreateOrderReq;
import com.example.shopapp.req.OrderItemReq;
import com.example.shopapp.ui.address.AddressListActivity;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.OrderPreviewVO;
import com.example.shopapp.vo.OrderVO;
import com.example.shopapp.vo.UserCouponVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 结算页（接口文档 8.2 订单预览 / 8.3 创建订单）：
 * 选择收货地址与优惠券、填写备注，提交后进入订单详情。
 * 购物车结算（fromCart=true）与商品详情「立即购买」（fromCart=false）共用。
 */
public class OrderConfirmActivity extends BaseActivity {

    private static final String EXTRA_ITEMS = "items";
    private static final String EXTRA_FROM_CART = "fromCart";

    private View layoutAddress;
    private TextView tvNoAddress, tvReceiver, tvAddress, tvCoupon, tvTotalAmount, tvDiscountAmount, tvPayAmount;
    private EditText etRemark;
    private OrderItemAdapter itemAdapter;

    private ArrayList<OrderItemReq> items;
    private boolean fromCart;
    private Long addressId;      // null 时由 DAO 取默认地址
    private Long userCouponId;   // null 表示不使用优惠券
    private OrderPreviewVO preview;
    private boolean submitting = false;

    /**
     * 打开结算页。
     *
     * @param items    购买商品列表
     * @param fromCart true 表示来自购物车结算，下单成功后删除对应购物车记录
     */
    public static void start(Context context, ArrayList<OrderItemReq> items, boolean fromCart) {
        Intent intent = new Intent(context, OrderConfirmActivity.class);
        intent.putExtra(EXTRA_ITEMS, items);
        intent.putExtra(EXTRA_FROM_CART, fromCart);
        context.startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);
        setupToolbar("确认订单", true);

        items = (ArrayList<OrderItemReq>) getIntent().getSerializableExtra(EXTRA_ITEMS);
        fromCart = getIntent().getBooleanExtra(EXTRA_FROM_CART, false);
        if (items == null || items.isEmpty()) {
            toast("没有可结算的商品");
            finish();
            return;
        }

        layoutAddress = findViewById(R.id.layout_address);
        tvNoAddress = findViewById(R.id.tv_no_address);
        tvReceiver = findViewById(R.id.tv_receiver);
        tvAddress = findViewById(R.id.tv_address);
        tvCoupon = findViewById(R.id.tv_coupon);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvDiscountAmount = findViewById(R.id.tv_discount_amount);
        tvPayAmount = findViewById(R.id.tv_pay_amount);
        etRemark = findViewById(R.id.et_remark);

        RecyclerView rvItems = findViewById(R.id.rv_items);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new OrderItemAdapter();
        rvItems.setAdapter(itemAdapter);

        layoutAddress.setOnClickListener(v -> chooseAddress());
        findViewById(R.id.layout_coupon).setOnClickListener(v -> chooseCoupon());
        findViewById(R.id.btn_submit).setOnClickListener(v -> submit());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从地址管理页返回后重新预览，刷新地址
        if (items != null && !items.isEmpty()) loadPreview();
    }

    /** 8.2 订单预览：按当前地址、优惠券计算金额 */
    private void loadPreview() {
        showLoading();
        final Long reqAddressId = addressId;
        final Long reqCouponId = userCouponId;
        ApiExecutor.run(() -> DaoFactory.order().previewOrder(userId(), items, reqAddressId, reqCouponId),
                new ApiCallback<OrderPreviewVO>() {
                    @Override
                    public void onSuccess(OrderPreviewVO data) {
                        hideLoading();
                        preview = data;
                        render(data);
                    }

                    @Override
                    public void onError(int code, String message) {
                        hideLoading();
                        // 所选地址已被删除 / 优惠券已不可用：清除后重新预览
                        if (code == ResultCode.ADDRESS_NOT_FOUND && reqAddressId != null) {
                            addressId = null;
                            loadPreview();
                            return;
                        }
                        if ((code == ResultCode.COUPON_UNUSABLE || code == ResultCode.COUPON_INVALID) && reqCouponId != null) {
                            userCouponId = null;
                            toast("优惠券不可用，已取消使用");
                            loadPreview();
                            return;
                        }
                        toast(message);
                        finish();
                    }
                });
    }

    private void render(OrderPreviewVO data) {
        Address address = data.getAddress();
        if (address == null) {
            addressId = null;
            tvNoAddress.setVisibility(View.VISIBLE);
            tvReceiver.setVisibility(View.GONE);
            tvAddress.setVisibility(View.GONE);
        } else {
            addressId = address.getId();
            tvNoAddress.setVisibility(View.GONE);
            tvReceiver.setVisibility(View.VISIBLE);
            tvAddress.setVisibility(View.VISIBLE);
            tvReceiver.setText(address.getReceiverName() + "  " + address.getReceiverPhone());
            tvAddress.setText(address.getFullAddress());
        }

        itemAdapter.setData(data.getItems());

        UserCouponVO coupon = data.getCoupon();
        if (coupon == null) {
            userCouponId = null;
            tvCoupon.setText("不使用优惠券");
        } else {
            userCouponId = coupon.getId();
            tvCoupon.setText(coupon.getName() + "（" + coupon.getDiscountText() + "）");
        }

        tvTotalAmount.setText(MoneyUtil.format(data.getTotalAmount()));
        tvDiscountAmount.setText("-" + MoneyUtil.format(data.getDiscountAmount()));
        tvPayAmount.setText(MoneyUtil.format(data.getPayAmount()));
    }

    /** 选择收货地址（7.1）：无地址则跳转地址管理页新增 */
    private void chooseAddress() {
        showLoading();
        ApiExecutor.run(() -> DaoFactory.address().listAddresses(userId()), new ApiCallback<List<Address>>() {
            @Override
            public void onSuccess(List<Address> data) {
                hideLoading();
                if (data == null || data.isEmpty()) {
                    toast("请先添加收货地址");
                    openAddressManage();
                    return;
                }
                String[] labels = new String[data.size()];
                int checked = -1;
                for (int i = 0; i < data.size(); i++) {
                    Address a = data.get(i);
                    labels[i] = a.getReceiverName() + "  " + a.getReceiverPhone()
                            + (a.isDefaultAddress() ? "（默认）" : "") + "\n" + a.getFullAddress();
                    if (addressId != null && addressId.equals(a.getId())) checked = i;
                }
                new AlertDialog.Builder(OrderConfirmActivity.this)
                        .setTitle("选择收货地址")
                        .setSingleChoiceItems(labels, checked, (d, which) -> {
                            addressId = data.get(which).getId();
                            d.dismiss();
                            loadPreview();
                        })
                        .setNeutralButton("管理地址", (d, w) -> openAddressManage())
                        .setNegativeButton("取消", null)
                        .show();
            }

            @Override
            public void onError(int code, String message) {
                hideLoading();
                toast(message);
            }
        });
    }

    private void openAddressManage() {
        startActivity(new Intent(this, AddressListActivity.class));
    }

    /** 选择优惠券（6.1.4 订单可用优惠券） */
    private void chooseCoupon() {
        if (preview == null) return;
        showLoading();
        ApiExecutor.run(() -> DaoFactory.coupon().listUsableCoupons(userId(), preview.getTotalAmount()),
                new ApiCallback<List<UserCouponVO>>() {
                    @Override
                    public void onSuccess(List<UserCouponVO> data) {
                        hideLoading();
                        List<UserCouponVO> coupons = data == null ? new ArrayList<>() : data;
                        String[] labels = new String[coupons.size() + 1];
                        labels[0] = "不使用优惠券";
                        int checked = 0;
                        for (int i = 0; i < coupons.size(); i++) {
                            UserCouponVO c = coupons.get(i);
                            labels[i + 1] = c.getName() + "（" + c.getDiscountText() + "）"
                                    + (c.getEndTime() == null ? "" : "\n有效期至 " + c.getEndTime());
                            if (userCouponId != null && userCouponId.equals(c.getId())) checked = i + 1;
                        }
                        new AlertDialog.Builder(OrderConfirmActivity.this)
                                .setTitle(coupons.isEmpty() ? "暂无可用优惠券" : "选择优惠券")
                                .setSingleChoiceItems(labels, checked, (d, which) -> {
                                    userCouponId = which == 0 ? null : coupons.get(which - 1).getId();
                                    d.dismiss();
                                    loadPreview();
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }

                    @Override
                    public void onError(int code, String message) {
                        hideLoading();
                        toast(message);
                    }
                });
    }

    /** 8.3 创建订单 */
    private void submit() {
        if (submitting) return;
        if (addressId == null) {
            toast("请选择收货地址");
            chooseAddress();
            return;
        }
        CreateOrderReq req = new CreateOrderReq();
        req.setAddressId(addressId);
        req.setItems(items);
        req.setUserCouponId(userCouponId);
        req.setRemark(etRemark.getText().toString().trim());
        req.setFromCart(fromCart);

        submitting = true;
        showLoading("提交中…");
        ApiExecutor.run(() -> DaoFactory.order().createOrder(userId(), req), new ApiCallback<OrderVO>() {
            @Override
            public void onSuccess(OrderVO data) {
                submitting = false;
                hideLoading();
                toast("下单成功");
                OrderDetailActivity.start(OrderConfirmActivity.this, data.getId());
                finish();
            }

            @Override
            public void onError(int code, String message) {
                submitting = false;
                hideLoading();
                toast(message);
                // 库存 / 优惠券 / 地址状态可能已变化，重新预览
                loadPreview();
            }
        });
    }
}
