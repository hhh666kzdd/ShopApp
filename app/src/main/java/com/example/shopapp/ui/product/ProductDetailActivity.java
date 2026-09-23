package com.example.shopapp.ui.product;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.req.OrderItemReq;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.ui.base.ImageLoader;
import com.example.shopapp.ui.order.OrderConfirmActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.ProductVO;

import java.util.ArrayList;

/**
 * 商品详情页（接口文档 4.2）：加入购物车（5.2）、立即购买（进入结算页）。
 */
public class ProductDetailActivity extends BaseActivity {

    private static final String EXTRA_PRODUCT_ID = "productId";

    private ImageView ivImage;
    private TextView tvPrice, tvOriginalPrice, tvPromoTag, tvName, tvSubtitle, tvCategory, tvSales, tvStock,
            tvQuantity, tvDetail;

    private Long productId;
    private ProductVO product;
    private int quantity = 1;

    public static void start(Context context, Long productId) {
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        setupToolbar("商品详情", true);
        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);

        ivImage = findViewById(R.id.iv_image);
        tvPrice = findViewById(R.id.tv_price);
        tvOriginalPrice = findViewById(R.id.tv_original_price);
        tvPromoTag = findViewById(R.id.tv_promo_tag);
        tvName = findViewById(R.id.tv_name);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvCategory = findViewById(R.id.tv_category);
        tvSales = findViewById(R.id.tv_sales);
        tvStock = findViewById(R.id.tv_stock);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvDetail = findViewById(R.id.tv_detail);

        findViewById(R.id.iv_minus).setOnClickListener(v -> changeQuantity(-1));
        findViewById(R.id.iv_plus).setOnClickListener(v -> changeQuantity(1));
        findViewById(R.id.btn_add_cart).setOnClickListener(v -> addToCart());
        findViewById(R.id.btn_buy).setOnClickListener(v -> buyNow());

        loadDetail();
    }

    private void loadDetail() {
        showLoading();
        ApiExecutor.run(() -> DaoFactory.product().getProductDetail(productId), new ApiCallback<ProductVO>() {
            @Override
            public void onSuccess(ProductVO data) {
                hideLoading();
                product = data;
                render();
            }

            @Override
            public void onError(int code, String message) {
                hideLoading();
                toast(message);
                finish();
            }
        });
    }

    private void render() {
        ImageLoader.load(this, product.getMainImage(), ivImage);
        tvPrice.setText(MoneyUtil.format(product.getCurrentPrice()));
        if (product.hasPromotion()) {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setText(MoneyUtil.format(product.getPrice()));
            tvOriginalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            tvPromoTag.setVisibility(View.VISIBLE);
            tvPromoTag.setText(product.getPromotionTitle() == null ? "限时特价" : product.getPromotionTitle());
        }
        tvName.setText(product.getName());
        tvSubtitle.setText(product.getSubtitle());
        tvSubtitle.setVisibility(product.getSubtitle() == null || product.getSubtitle().isEmpty() ? View.GONE : View.VISIBLE);
        tvCategory.setText("分类：" + (product.getCategoryName() == null ? "-" : product.getCategoryName()));
        tvSales.setText("销量：" + (product.getSales() == null ? 0 : product.getSales()));
        tvStock.setText("库存：" + (product.getStock() == null ? 0 : product.getStock()));
        tvDetail.setText(product.getDetail() == null || product.getDetail().isEmpty() ? "暂无描述" : product.getDetail());
    }

    private void changeQuantity(int delta) {
        int max = product == null || product.getStock() == null ? 1 : product.getStock();
        int q = quantity + delta;
        if (q < 1) q = 1;
        if (q > max) {
            q = Math.max(max, 1);
            toast("已达库存上限");
        }
        quantity = q;
        tvQuantity.setText(String.valueOf(quantity));
    }

    private boolean checkStock() {
        if (product == null) return false;
        if (product.getStock() == null || product.getStock() <= 0) {
            toast("商品库存不足");
            return false;
        }
        return true;
    }

    private void addToCart() {
        if (!checkStock()) return;
        showLoading();
        ApiExecutor.run(() -> DaoFactory.cart().addToCart(userId(), productId, quantity), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                hideLoading();
                toast("已加入购物车");
            }

            @Override
            public void onError(int code, String message) {
                hideLoading();
                toast(message);
            }
        });
    }

    /** 立即购买：直接携带商品进入结算页，不经过购物车 */
    private void buyNow() {
        if (!checkStock()) return;
        ArrayList<OrderItemReq> items = new ArrayList<>();
        items.add(new OrderItemReq(productId, quantity));
        OrderConfirmActivity.start(this, items, false);
    }
}
