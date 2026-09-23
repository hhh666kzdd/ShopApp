package com.example.shopapp.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.req.OrderItemReq;
import com.example.shopapp.ui.order.OrderConfirmActivity;
import com.example.shopapp.ui.product.ProductDetailActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.util.ToastUtil;
import com.example.shopapp.vo.CartItemVO;
import com.example.shopapp.vo.CartSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车页（接口文档 5）：勾选、修改数量、删除、全选、合计、结算。
 */
public class CartFragment extends Fragment implements CartAdapter.Listener {

    private CartAdapter adapter;
    private View emptyView;
    private CheckBox cbAll;
    private TextView tvTotal;
    private Button btnCheckout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        emptyView = view.findViewById(R.id.empty_view);
        cbAll = view.findViewById(R.id.cb_all);
        tvTotal = view.findViewById(R.id.tv_total);
        btnCheckout = view.findViewById(R.id.btn_checkout);

        RecyclerView rv = view.findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(this);
        rv.setAdapter(adapter);

        cbAll.setOnClickListener(v -> {
            boolean checked = cbAll.isChecked();
            ApiExecutor.run(() -> DaoFactory.cart().checkAll(userId(), checked ? 1 : 0), new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    loadCart();
                }
            });
        });
        btnCheckout.setOnClickListener(v -> checkout());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    /** Tab 被重新显示时刷新（MainActivity 使用 show/hide 切换） */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadCart();
    }

    private Long userId() {
        return SessionManager.getUserId();
    }

    private void loadCart() {
        ApiExecutor.run(() -> DaoFactory.cart().listCart(userId()), new ApiCallback<List<CartItemVO>>() {
            @Override
            public void onSuccess(List<CartItemVO> data) {
                adapter.setData(data);
                emptyView.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
                // 全选框状态：所有可购买商品都已勾选
                boolean all = !adapter.isEmpty();
                for (CartItemVO item : adapter.getData()) {
                    if (item.getStatus() != null && item.getStatus() == 1 && !item.isChecked()) {
                        all = false;
                        break;
                    }
                }
                cbAll.setChecked(all);
                refreshSummary();
            }

            @Override
            public void onError(int code, String message) {
                ToastUtil.show(message);
            }
        });
    }

    /** 已勾选合计（接口文档 5.8） */
    private void refreshSummary() {
        ApiExecutor.run(() -> DaoFactory.cart().getCheckedSummary(userId()), new ApiCallback<CartSummary>() {
            @Override
            public void onSuccess(CartSummary data) {
                tvTotal.setText("合计：" + MoneyUtil.format(data.getTotalAmount()));
                btnCheckout.setText("结算(" + data.getTotalCount() + ")");
            }
        });
    }

    @Override
    public void onCheckedChange(CartItemVO item, boolean checked) {
        ApiExecutor.run(() -> DaoFactory.cart().updateChecked(userId(), item.getId(), checked ? 1 : 0), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                item.setChecked(checked ? 1 : 0);
                refreshSummary();
            }

            @Override
            public void onError(int code, String message) {
                ToastUtil.show(message);
                loadCart();
            }
        });
    }

    @Override
    public void onQuantityChange(CartItemVO item, int newQuantity) {
        ApiExecutor.run(() -> DaoFactory.cart().updateQuantity(userId(), item.getId(), newQuantity), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadCart();
            }

            @Override
            public void onError(int code, String message) {
                ToastUtil.show(message);
            }
        });
    }

    @Override
    public void onDelete(CartItemVO item) {
        new AlertDialog.Builder(requireContext())
                .setMessage("确定删除「" + item.getProductName() + "」？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (d, w) ->
                        ApiExecutor.run(() -> DaoFactory.cart().deleteCartItem(userId(), item.getId()), new ApiCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                loadCart();
                            }
                        }))
                .show();
    }

    @Override
    public void onItemClick(CartItemVO item) {
        ProductDetailActivity.start(requireContext(), item.getProductId());
    }

    /** 结算：把已勾选且上架的商品带到结算页 */
    private void checkout() {
        ArrayList<OrderItemReq> items = new ArrayList<>();
        for (CartItemVO item : adapter.getData()) {
            if (item.isChecked() && item.getStatus() != null && item.getStatus() == 1) {
                items.add(new OrderItemReq(item.getProductId(), item.getQuantity()));
            }
        }
        if (items.isEmpty()) {
            ToastUtil.show("请先勾选要结算的商品");
            return;
        }
        OrderConfirmActivity.start(requireContext(), items, true);
    }
}
