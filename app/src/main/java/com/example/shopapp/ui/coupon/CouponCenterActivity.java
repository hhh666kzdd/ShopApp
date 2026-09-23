package com.example.shopapp.ui.coupon;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.vo.CouponVO;

import java.util.List;

/**
 * 领券中心（接口文档 6.1.1 / 6.1.2）：展示可领取优惠券并领取。
 */
public class CouponCenterActivity extends BaseActivity {

    private CouponAdapter adapter;
    private View emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_center);
        setupToolbar("领券中心", true);

        emptyView = findViewById(R.id.empty_view);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CouponAdapter(this::receive);
        rv.setAdapter(adapter);

        loadCoupons();
    }

    private void loadCoupons() {
        ApiExecutor.run(() -> DaoFactory.coupon().listAvailableCoupons(userId()), new ApiCallback<List<CouponVO>>() {
            @Override
            public void onSuccess(List<CouponVO> data) {
                adapter.setData(data);
                toggleEmpty(emptyView, adapter.isEmpty());
            }

            @Override
            public void onError(int code, String message) {
                toast(message);
            }
        });
    }

    private void receive(CouponVO coupon) {
        showLoading("领取中…");
        ApiExecutor.run(() -> DaoFactory.coupon().receiveCoupon(userId(), coupon.getId()), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                hideLoading();
                toast("领取成功");
                loadCoupons();
            }

            @Override
            public void onError(int code, String message) {
                hideLoading();
                toast(message);
            }
        });
    }
}
