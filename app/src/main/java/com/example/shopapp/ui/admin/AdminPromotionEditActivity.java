package com.example.shopapp.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.entity.Promotion;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.DateUtil;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.vo.ProductVO;
import com.example.shopapp.vo.PromotionVO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * 后台限时特价活动新增 / 编辑【管理员】（接口文档 6.2.3 addPromotion / updatePromotion）。
 */
public class AdminPromotionEditActivity extends BaseActivity {

    private static final String EXTRA_PROMOTION = "promotion";

    private EditText etProductId, etTitle, etDescription, etPromoPrice, etStartTime, etEndTime;
    private TextView tvProductInfo;
    private RadioGroup rgStatus;

    /** 编辑时的原对象，为 null 表示新增 */
    private PromotionVO editing;

    /**
     * @param promotion 为 null 表示新增，否则编辑该活动
     */
    public static void start(Context context, PromotionVO promotion) {
        Intent intent = new Intent(context, AdminPromotionEditActivity.class);
        if (promotion != null) intent.putExtra(EXTRA_PROMOTION, promotion);
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
        setContentView(R.layout.activity_admin_promotion_edit);
        editing = (PromotionVO) getIntent().getSerializableExtra(EXTRA_PROMOTION);
        setupToolbar(editing == null ? "新增活动" : "编辑活动", true);

        etProductId = findViewById(R.id.et_product_id);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPromoPrice = findViewById(R.id.et_promo_price);
        etStartTime = findViewById(R.id.et_start_time);
        etEndTime = findViewById(R.id.et_end_time);
        tvProductInfo = findViewById(R.id.tv_product_info);
        rgStatus = findViewById(R.id.rg_status);

        findViewById(R.id.btn_query).setOnClickListener(v -> queryProduct());
        findViewById(R.id.btn_save).setOnClickListener(v -> save());

        if (editing == null) {
            fillDefaults();
        } else {
            fill(editing);
        }
    }

    /** 新增时的默认值：活动期一个月 */
    private void fillDefaults() {
        etStartTime.setText(DateUtil.now());
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 1);
        etEndTime.setText(DateUtil.format(c.getTime()));
    }

    private void fill(PromotionVO p) {
        etProductId.setText(p.getProductId() == null ? "" : String.valueOf(p.getProductId()));
        tvProductInfo.setText(buildProductInfo(p.getProductName(), p.getOriginalPrice()));
        etTitle.setText(p.getTitle());
        etDescription.setText(p.getDescription());
        etPromoPrice.setText(p.getPromoPrice() == null ? "" : MoneyUtil.plain(p.getPromoPrice()));
        etStartTime.setText(p.getStartTime());
        etEndTime.setText(p.getEndTime());
        rgStatus.check(p.getStatus() != null && p.getStatus() == 0 ? R.id.rb_disabled : R.id.rb_enabled);
    }

    private static String buildProductInfo(String name, BigDecimal price) {
        if (name == null) return "输入商品 ID 后点击「查询」确认商品";
        return "商品：" + name + "　原价：" + MoneyUtil.format(price);
    }

    private Long parseProductId() {
        String s = etProductId.getText().toString().trim();
        if (s.isEmpty()) return null;
        try {
            long id = Long.parseLong(s);
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 按商品 ID 查询商品名称与原价（接口文档 4.2），辅助确认活动商品 */
    private void queryProduct() {
        Long productId = parseProductId();
        if (productId == null) {
            toast("请输入正确的商品 ID");
            return;
        }
        showLoading("查询中…");
        ApiExecutor.run(() -> DaoFactory.product().getProductDetail(productId), new ApiCallback<ProductVO>() {
            @Override
            public void onSuccess(ProductVO data) {
                hideLoading();
                tvProductInfo.setText(buildProductInfo(data.getName(), data.getPrice())
                        + (data.isOnSale() ? "" : "（已下架）"));
            }

            @Override
            public void onError(int code, String message) {
                hideLoading();
                tvProductInfo.setText("未找到该商品：" + message);
                toast(message);
            }
        });
    }

    private void save() {
        Long productId = parseProductId();
        if (productId == null) {
            toast("请输入正确的商品 ID");
            return;
        }
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            toast("请输入活动标题");
            return;
        }
        BigDecimal promoPrice = MoneyUtil.parse(etPromoPrice.getText().toString());
        if (!MoneyUtil.isPositive(promoPrice)) {
            toast("请输入正确的活动价");
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
            toast("结束时间必须晚于开始时间");
            return;
        }

        Promotion p = new Promotion();
        p.setId(editing == null ? null : editing.getId());
        p.setProductId(productId);
        p.setTitle(title);
        p.setDescription(etDescription.getText().toString().trim());
        p.setPromoPrice(MoneyUtil.scale(promoPrice));
        p.setStartTime(startTime);
        p.setEndTime(endTime);
        p.setStatus(rgStatus.getCheckedRadioButtonId() == R.id.rb_disabled ? 0 : 1);

        showLoading("保存中…");
        ApiExecutor.run(() -> editing == null
                        ? DaoFactory.promotion().addPromotion(userId(), p)
                        : DaoFactory.promotion().updatePromotion(userId(), p),
                new ApiCallback<PromotionVO>() {
                    @Override
                    public void onSuccess(PromotionVO data) {
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
