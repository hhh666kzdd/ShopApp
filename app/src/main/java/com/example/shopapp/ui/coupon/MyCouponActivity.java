package com.example.shopapp.ui.coupon;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.vo.UserCouponVO;

import java.util.List;

/**
 * 我的优惠券（接口文档 6.1.3）：按未使用 / 已使用 / 已过期分类查看。
 */
public class MyCouponActivity extends BaseActivity {

    private MyCouponAdapter adapter;
    private View emptyView;
    private int status = UserCouponVO.STATUS_UNUSED;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_coupon);
        setupToolbar("我的优惠券", true);

        emptyView = findViewById(R.id.empty_view);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyCouponAdapter();
        rv.setAdapter(adapter);

        RadioGroup rgStatus = findViewById(R.id.rg_status);
        rgStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_used) {
                status = UserCouponVO.STATUS_USED;
            } else if (checkedId == R.id.rb_expired) {
                status = UserCouponVO.STATUS_EXPIRED;
            } else {
                status = UserCouponVO.STATUS_UNUSED;
            }
            loadCoupons();
        });

        loadCoupons();
    }

    private void loadCoupons() {
        final int queryStatus = status;
        ApiExecutor.run(() -> DaoFactory.coupon().listMyCoupons(userId(), queryStatus), new ApiCallback<List<UserCouponVO>>() {
            @Override
            public void onSuccess(List<UserCouponVO> data) {
                if (queryStatus != status) return; // 切换 Tab 后到达的旧结果丢弃
                adapter.setData(data);
                toggleEmpty(emptyView, adapter.isEmpty());
            }

            @Override
            public void onError(int code, String message) {
                toast(message);
            }
        });
    }
}
