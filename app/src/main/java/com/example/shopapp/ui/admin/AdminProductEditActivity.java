package com.example.shopapp.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.entity.Category;
import com.example.shopapp.entity.Product;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.util.ValidateUtil;
import com.example.shopapp.vo.ProductVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 后台新增 / 编辑商品【管理员】（接口文档 4.4 / 4.5）。
 */
public class AdminProductEditActivity extends BaseActivity {

    private static final String EXTRA_PRODUCT = "product";

    private Spinner spCategory;
    private EditText etName, etSubtitle, etImage, etDetail, etPrice, etStock;
    private RadioGroup rgStatus;

    private final List<Category> categories = new ArrayList<>();
    private ArrayAdapter<Category> categoryAdapter;
    private ProductVO product; // null 表示新增

    /**
     * @param product 待编辑商品，null 表示新增
     */
    public static void start(Context context, ProductVO product) {
        Intent intent = new Intent(context, AdminProductEditActivity.class);
        if (product != null) intent.putExtra(EXTRA_PRODUCT, product);
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
        setContentView(R.layout.activity_admin_product_edit);
        product = (ProductVO) getIntent().getSerializableExtra(EXTRA_PRODUCT);
        setupToolbar(product == null ? "新增商品" : "编辑商品", true);

        spCategory = findViewById(R.id.sp_category);
        etName = findViewById(R.id.et_name);
        etSubtitle = findViewById(R.id.et_subtitle);
        etImage = findViewById(R.id.et_image);
        etDetail = findViewById(R.id.et_detail);
        etPrice = findViewById(R.id.et_price);
        etStock = findViewById(R.id.et_stock);
        rgStatus = findViewById(R.id.rg_status);
        findViewById(R.id.btn_save).setOnClickListener(v -> save());

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        if (product != null) {
            etName.setText(product.getName());
            etSubtitle.setText(product.getSubtitle());
            etImage.setText(product.getMainImage());
            etDetail.setText(product.getDetail());
            etPrice.setText(MoneyUtil.plain(product.getPrice()));
            etStock.setText(String.valueOf(product.getStock() == null ? 0 : product.getStock()));
            rgStatus.check(product.isOnSale() ? R.id.rb_on : R.id.rb_off);
        }
        loadCategories();
    }

    /** 分类下拉：含停用分类，编辑时选中原分类 */
    private void loadCategories() {
        ApiExecutor.run(() -> DaoFactory.category().listAllCategories(userId()), new ApiCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                categories.clear();
                if (data != null) categories.addAll(data);
                categoryAdapter.notifyDataSetChanged();
                if (product != null && product.getCategoryId() != null) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (product.getCategoryId().equals(categories.get(i).getId())) {
                            spCategory.setSelection(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onError(int code, String message) {
                toast(message);
            }
        });
    }

    private void save() {
        Category category = (Category) spCategory.getSelectedItem();
        String name = etName.getText().toString().trim();
        String subtitle = etSubtitle.getText().toString().trim();
        String image = etImage.getText().toString().trim();
        String detail = etDetail.getText().toString().trim();
        BigDecimal price = MoneyUtil.parse(etPrice.getText().toString());
        Integer stock = ValidateUtil.parseInt(etStock.getText().toString());

        if (category == null) {
            toast("请选择所属分类");
            return;
        }
        if (name.isEmpty()) {
            toast("商品名称不能为空");
            return;
        }
        if (!MoneyUtil.isPositive(price)) {
            toast("原价必须大于 0");
            return;
        }
        if (stock == null || stock < 0) {
            toast("库存必须为不小于 0 的整数");
            return;
        }

        Product p = new Product();
        if (product != null) p.setId(product.getId());
        p.setCategoryId(category.getId());
        p.setName(name);
        p.setSubtitle(subtitle.isEmpty() ? null : subtitle);
        p.setMainImage(image.isEmpty() ? null : image);
        p.setDetail(detail.isEmpty() ? null : detail);
        p.setPrice(MoneyUtil.scale(price));
        p.setStock(stock);
        p.setStatus(rgStatus.getCheckedRadioButtonId() == R.id.rb_off ? 0 : 1);

        showLoading("保存中…");
        ApiExecutor.run(() -> product == null
                        ? DaoFactory.product().addProduct(userId(), p)
                        : DaoFactory.product().updateProduct(userId(), p),
                new ApiCallback<ProductVO>() {
                    @Override
                    public void onSuccess(ProductVO data) {
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
