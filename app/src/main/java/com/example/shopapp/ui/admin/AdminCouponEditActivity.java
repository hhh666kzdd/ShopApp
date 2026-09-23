package com.example.shopapp.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.entity.Coupon;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.DateUtil;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.util.ValidateUtil;
import com.example.shopapp.vo.CouponVO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * 后台优惠券新增 / 编辑【管理员】（接口文档 6.1.6 addCoupon / updateCoupon）。
 */
public class AdminCouponEditActivity extends BaseActivity {

    private static final String EXTRA_COUPON = "coupon";

    private EditText etName, etThreshold, etDiscountAmount, etDiscountRate, etTotal, etPerLimit, etStartTime, etEndTime;
    private RadioGroup rgType, rgStatus;
    private View layoutCash, layoutRate;

    /** 编辑时的原对象，为 null 表示新增 */
    private CouponVO editing;

    /**
     * @param coupon 为 null 表示新增，否则编辑该优惠券
     */
    public static void start(Context context, CouponVO coupon) {
        Intent intent = new Intent(context, AdminCouponEditActivity.class);
        if (coupon != null) intent.putExtra(EXTRA_COUPON, coupon);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isAdmin()) {
            toast("无权限");
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_coupon_edit);
        editing = (CouponVO) getIntent().getSerializableExtra(EXTRA_COUPON);
        setupToolbar(editing == null ? "新增优惠券" : "编辑优惠券", true);

        etName = findViewById(R.id.et_name);
        etThreshold = findViewById(R.id.et_threshold);
        etDiscountAmount = findViewById(R.id.et_discount_amount);
        etDiscountRate = findViewById(R.id.et_discount_rate);
        etTotal = findViewById(R.id.et_total);
        etPerLimit = findViewById(R.id.et_per_limit);
        etStartTime = findViewById(R.id.et_start_time);
        etEndTime = findViewById(R.id.et_end_time);
        rgType = findViewById(R.id.rg_type);
        rgStatus = findViewById(R.id.rg_status);
        layoutCash = findViewById(R.id.layout_cash);
        layoutRate = findViewById(R.id.layout_rate);

        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean rate = checkedId == R.id.rb_rate;
            layoutCash.setVisibility(rate ? View.GONE : View.VISIBLE);
            layoutRate.setVisibility(rate ? View.VISIBLE : View.GONE);
        });
        findViewById(R.id.btn_save).setOnClickListener(v -> save());

        if (editing == null) {
            fillDefaults();
        } else {
            fill(editing);
        }
    }

    /** 新增时的默认值：有效期从现在起一年 */
    private void fillDefaults() {
        etThreshold.setText("0");
        etPerLimit.setText("1");
        etStartTime.setText(DateUtil.now());
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        etEndTime.setText(DateUtil.format(c.getTime()));
    }

    private void fill(Coupon c) {
        etName.setText(c.getName());
        boolean rate = c.getType() != null && c.getType() == Coupon.TYPE_RATE;
        rgType.check(rate ? R.id.rb_rate : R.id.rb_cash);
        etThreshold.setText(MoneyUtil.plain(c.getThreshold()));
        etDiscountAmount.setText(c.getDiscountAmount() == null ? "" : MoneyUtil.plain(c.getDiscountAmount()));
        etDiscountRate.setText(c.getDiscountRate() == null ? "" : c.getDiscountRate().stripTrailingZeros().toPlainString());
        etTotal.setText(c.getTotal() == null ? "" : String.valueOf(c.getTotal()));
        etPerLimit.setText(c.getPerLimit() == null ? "1" : String.valueOf(c.getPerLimit()));
        etStartTime.setText(c.getStartTime());
        etEndTime.setText(c.getEndTime());
        rgStatus.check(c.getStatus() != null && c.getStatus() == 0 ? R.id.rb_disabled : R.id.rb_enabled);
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            toast("请输入优惠券名称");
            return;
        }
        boolean rateType = rgType.getCheckedRadioButtonId() == R.id.rb_rate;

        BigDecimal threshold = MoneyUtil.parse(etThreshold.getText().toString());
        if (threshold == null) threshold = BigDecimal.ZERO;
        if (threshold.compareTo(BigDecimal.ZERO) < 0) {
            toast("使用门槛不能为负数");
            return;
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discountRate = BigDecimal.ONE;
        if (rateType) {
            discountRate = MoneyUtil.parse(etDiscountRate.getText().toString());
            if (discountRate == null || discountRate.compareTo(new BigDecimal("0.01")) < 0
                    || discountRate.compareTo(new BigDecimal("0.99")) > 0) {
                toast("折扣率须在 0.01 ~ 0.99 之间");
                return;
            }
        } else {
            discountAmount = MoneyUtil.parse(etDiscountAmount.getText().toString());
            if (!MoneyUtil.isPositive(discountAmount)) {
                toast("请输入正确的满减金额");
                return;
            }
        }

        Integer total = ValidateUtil.parseInt(etTotal.getText().toString());
        if (total == null || total <= 0) {
            toast("请输入正确的发放总量");
            return;
        }
        Integer perLimit = ValidateUtil.parseInt(etPerLimit.getText().toString());
        if (perLimit == null || perLimit <= 0) {
            toast("请输入正确的每人限领张数");
            return;
        }

        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        Timestamp start = DateUtil.parse(startTime);
        Timestamp end = DateUtil.parse(endTime);
        if (start == null || end == null) {
            toast("时间格式应为 yyyy-MM-dd HH:mm:ss");
            return;
        }
        if (!end.after(start)) {
            toast("失效时间必须晚于生效时间");
            return;
        }

        Coupon c = new Coupon();
        c.setId(editing == null ? null : editing.getId());
        c.setName(name);
        c.setType(rateType ? Coupon.TYPE_RATE : Coupon.TYPE_CASH);
        c.setThreshold(MoneyUtil.scale(threshold));
        c.setDiscountAmount(MoneyUtil.scale(discountAmount));
        c.setDiscountRate(discountRate);
        c.setTotal(total);
        // 编辑时保留剩余数量（DAO 新增时 remain 初始等于 total）
        c.setRemain(editing == null ? total : editing.getRemain());
        c.setPerLimit(perLimit);
        c.setStartTime(startTime);
        c.setEndTime(endTime);
        c.setStatus(rgStatus.getCheckedRadioButtonId() == R.id.rb_disabled ? 0 : 1);

        showLoading("保存中…");
        ApiExecutor.run(() -> editing == null
                        ? DaoFactory.coupon().addCoupon(userId(), c)
                        : DaoFactory.coupon().updateCoupon(userId(), c),
                new ApiCallback<CouponVO>() {
                    @Override
                    public void onSuccess(CouponVO data) {
                        hideLoading();
                        toast("保存成功");
                        finish();
                    }

                    @Override
                    public void onError(int code, String message) {
                        hideLoading();
                        toast(message);
                    }
                });
    }
}
