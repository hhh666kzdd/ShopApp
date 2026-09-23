package com.example.shopapp.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.ui.main.MainActivity;
import com.example.shopapp.ui.user.LoginActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.vo.StatisticsVO;

/**
 * 后台管理首页【管理员】：销售统计（接口文档 9.4）+ 各管理模块入口。
 */
public class AdminMainActivity extends BaseActivity {

    private TextView tvTotalSales, tvTodaySales, tvOrderCount, tvTodayOrderCount,
            tvPendingShipCount, tvUserCount, tvProductCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isAdmin()) {
            toast("无权限");
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_main);
        setupToolbar("后台管理", false);

        tvTotalSales = findViewById(R.id.tv_total_sales);
        tvTodaySales = findViewById(R.id.tv_today_sales);
        tvOrderCount = findViewById(R.id.tv_order_count);
        tvTodayOrderCount = findViewById(R.id.tv_today_order_count);
        tvPendingShipCount = findViewById(R.id.tv_pending_ship_count);
        tvUserCount = findViewById(R.id.tv_user_count);
        tvProductCount = findViewById(R.id.tv_product_count);

        setupEntry(R.id.item_product, R.drawable.ic_cart, "商品管理", v -> open(AdminProductListActivity.class));
        setupEntry(R.id.item_category, R.drawable.ic_category, "分类管理", v -> open(AdminCategoryActivity.class));
        setupEntry(R.id.item_coupon, R.drawable.ic_coupon, "优惠券管理", v -> open(AdminCouponListActivity.class));
        setupEntry(R.id.item_promotion, R.drawable.ic_flash, "特价活动", v -> open(AdminPromotionListActivity.class));
        setupEntry(R.id.item_order, R.drawable.ic_order, "订单管理", v -> open(AdminOrderListActivity.class));
        setupEntry(R.id.item_user, R.drawable.ic_admin, "用户管理", v -> open(AdminUserListActivity.class));
        setupEntry(R.id.item_shop, R.drawable.ic_home, "进入商城", v -> open(MainActivity.class));
        setupEntry(R.id.item_logout, R.drawable.ic_logout, "退出登录", v -> logout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionManager.isAdmin()) loadStatistics();
    }

    private void loadStatistics() {
        ApiExecutor.run(() -> DaoFactory.admin().getStatistics(userId()), new ApiCallback<StatisticsVO>() {
            @Override
            public void onSuccess(StatisticsVO data) {
                tvTotalSales.setText(MoneyUtil.format(data.getTotalSales()));
                tvTodaySales.setText(MoneyUtil.format(data.getTodaySales()));
                tvOrderCount.setText(String.valueOf(n(data.getOrderCount())));
                tvTodayOrderCount.setText(String.valueOf(n(data.getTodayOrderCount())));
                tvPendingShipCount.setText(String.valueOf(n(data.getPendingShipCount())));
                tvUserCount.setText(String.valueOf(n(data.getUserCount())));
                tvProductCount.setText(String.valueOf(n(data.getProductCount())));
            }

            @Override
            public void onError(int code, String message) {
                toast(message);
            }
        });
    }

    private static int n(Integer value) {
        return value == null ? 0 : value;
    }

    private void setupEntry(int id, int iconRes, String title, View.OnClickListener listener) {
        View entry = findViewById(id);
        ((ImageView) entry.findViewById(R.id.iv_icon)).setImageResource(iconRes);
        ((TextView) entry.findViewById(R.id.tv_title)).setText(title);
        entry.setOnClickListener(listener);
    }

    private void open(Class<?> activity) {
        startActivity(new Intent(this, activity));
    }

    /** 退出登录（接口文档 2.6）：清除本地会话后回到登录页 */
    private void logout() {
        new AlertDialog.Builder(this)
                .setMessage("确定退出登录？")
                .setNegativeButton("取消", null)
                .setPositiveButton("退出", (d, w) -> {
                    DaoFactory.user().logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}
