package com.example.shopapp.ui.product;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.common.PageResult;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.dao.ProductDao;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.ui.base.LoadMoreListener;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.vo.ProductVO;

/**
 * 商品列表 / 搜索页（接口文档 4.1）：支持分类、关键字、排序、分页。
 */
public class ProductListActivity extends BaseActivity {

    private static final String EXTRA_CATEGORY_ID = "categoryId";
    private static final String EXTRA_KEYWORD = "keyword";
    private static final String EXTRA_TITLE = "title";
    private static final int PAGE_SIZE = 10;

    private EditText etSearch;
    private ProductAdapter adapter;
    private View emptyView;

    private Long categoryId;
    private String keyword;
    private String sort = ProductDao.SORT_DEFAULT;
    private int page = 1;
    private boolean hasMore = true;
    private boolean loading = false;

    /**
     * 打开商品列表。
     *
     * @param categoryId 分类 ID，可为 null
     * @param keyword    搜索关键字，可为 null
     * @param title      页面标题
     */
    public static void start(Context context, Long categoryId, String keyword, String title) {
        Intent intent = new Intent(context, ProductListActivity.class);
        if (categoryId != null) intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        intent.putExtra(EXTRA_KEYWORD, keyword);
        intent.putExtra(EXTRA_TITLE, title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        long cid = getIntent().getLongExtra(EXTRA_CATEGORY_ID, -1);
        categoryId = cid > 0 ? cid : null;
        keyword = getIntent().getStringExtra(EXTRA_KEYWORD);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        setupToolbar(title == null ? "商品列表" : title, true);

        etSearch = findViewById(R.id.et_search);
        etSearch.setText(keyword);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });
        findViewById(R.id.tv_search).setOnClickListener(v -> doSearch());

        RadioGroup rgSort = findViewById(R.id.rg_sort);
        rgSort.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_sales) {
                sort = ProductDao.SORT_SALES;
            } else if (checkedId == R.id.rb_price_asc) {
                sort = ProductDao.SORT_PRICE_ASC;
            } else if (checkedId == R.id.rb_price_desc) {
                sort = ProductDao.SORT_PRICE_DESC;
            } else {
                sort = ProductDao.SORT_DEFAULT;
            }
            load(true);
        });

        emptyView = findViewById(R.id.empty_view);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(p -> ProductDetailActivity.start(this, p.getId()));
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new LoadMoreListener(() -> {
            if (hasMore && !loading) load(false);
        }));

        load(true);
    }

    private void doSearch() {
        keyword = etSearch.getText().toString().trim();
        load(true);
    }

    private void load(boolean refresh) {
        if (refresh) {
            page = 1;
            hasMore = true;
        }
        loading = true;
        final int requestPage = page;
        ApiExecutor.run(() -> DaoFactory.product().listProducts(categoryId, keyword, sort, requestPage, PAGE_SIZE),
                new ApiCallback<PageResult<ProductVO>>() {
                    @Override
                    public void onSuccess(PageResult<ProductVO> data) {
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
}
