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
import com.example.shopapp.entity.Coupon;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.ui.base.LoadMoreListener;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.vo.CouponVO;

/**
 * 后台优惠券列表【管理员】（接口文档 6.1.6）：分页展示，支持新增、编辑、启用 / 停用、删除。
 */
public class AdminCouponListActivity extends BaseActivity implements AdminCouponAdapter.Listener {

    private static final int PAGE_SIZE = 10;

    private AdminCouponAdapter adapter;
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
        setContentView(R.layout.activity_admin_coupon_list);
        setupToolbar("优惠券管理", true);

        emptyView = findViewById(R.id.empty_view);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCouponAdapter(this);
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new LoadMoreListener(() -> {
            if (hasMore && !loading) load(false);
        }));

        findViewById(R.id.btn_add).setOnClickListener(v -> AdminCouponEditActivity.start(this, null));
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
        ApiExecutor.run(() -> DaoFactory.coupon().listAllCoupons(userId(), requestPage, PAGE_SIZE),
                new ApiCallback<PageResult<CouponVO>>() {
                    @Override
                    public void onSuccess(PageResult<CouponVO> data) {
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
    public void onEdit(CouponVO coupon) {
        AdminCouponEditActivity.start(this, coupon);
    }

    /** 启用 / 停用：只改 status，其余字段原样回传 */
    @Override
    public void onToggleStatus(CouponVO coupon) {
        boolean enabled = coupon.getStatus() != null && coupon.getStatus() == 1;
        Coupon c = copy(coupon);
        c.setStatus(enabled ? 0 : 1);
        showLoading();
        ApiExecutor.run(() -> DaoFactory.coupon().updateCoupon(userId(), c), new ApiCallback<CouponVO>() {
            @Override
            public void onSuccess(CouponVO data) {
                hideLoading();
                toast(enabled ? "已停用" : "已启用");
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
    public void onDelete(CouponVO coupon) {
        new AlertDialog.Builder(this)
                .setMessage("确定删除「" + coupon.getName() + "」？已被领取的优惠券只能停用。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (d, w) -> {
                    showLoading();
                    ApiExecutor.run(() -> DaoFactory.coupon().deleteCoupon(userId(), coupon.getId()), new ApiCallback<Void>() {
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

    /** 复制为入参实体，避免修改列表中对象后请求失败导致界面状态不一致 */
    private static Coupon copy(CouponVO vo) {
        Coupon c = new Coupon();
        c.setId(vo.getId());
        c.setName(vo.getName());
        c.setType(vo.getType());
        c.setThreshold(vo.getThreshold());
        c.setDiscountAmount(vo.getDiscountAmount());
        c.setDiscountRate(vo.getDiscountRate());
        c.setTotal(vo.getTotal());
        c.setRemain(vo.getRemain());
        c.setPerLimit(vo.getPerLimit());
        c.setStartTime(vo.getStartTime());
        c.setEndTime(vo.getEndTime());
        c.setStatus(vo.getStatus());
        return c;
    }
}
