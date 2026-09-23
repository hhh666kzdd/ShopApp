package com.example.shopapp.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.common.PageResult;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.dao.ProductDao;
import com.example.shopapp.entity.Category;
import com.example.shopapp.ui.base.LoadMoreListener;
import com.example.shopapp.ui.product.ProductAdapter;
import com.example.shopapp.ui.product.ProductDetailActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.ToastUtil;
import com.example.shopapp.vo.ProductVO;

import java.util.List;

/**
 * 分类页：左侧分类列表（3.1），右侧该分类下的商品（4.1，分页加载）。
 */
public class CategoryFragment extends Fragment {

    private static final int PAGE_SIZE = 10;

    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private View emptyView;

    private Long currentCategoryId;
    private int page = 1;
    private boolean hasMore = true;
    private boolean loading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        emptyView = view.findViewById(R.id.empty_view);

        RecyclerView rvCategory = view.findViewById(R.id.rv_category);
        rvCategory.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryAdapter = new CategoryAdapter((category, position) -> loadProducts(category.getId(), true));
        rvCategory.setAdapter(categoryAdapter);

        RecyclerView rvProduct = view.findViewById(R.id.rv_product);
        rvProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(p -> ProductDetailActivity.start(requireContext(), p.getId()));
        rvProduct.setAdapter(productAdapter);
        rvProduct.addOnScrollListener(new LoadMoreListener(() -> {
            if (hasMore && !loading && currentCategoryId != null) loadProducts(currentCategoryId, false);
        }));

        loadCategories();
    }

    private void loadCategories() {
        ApiExecutor.run(() -> DaoFactory.category().listCategories(), new ApiCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                categoryAdapter.setData(data);
                if (data != null && !data.isEmpty()) {
                    loadProducts(data.get(0).getId(), true);
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(int code, String message) {
                ToastUtil.show(message);
            }
        });
    }

    /**
     * 加载商品。
     *
     * @param refresh true 表示切换分类，从第一页重新加载
     */
    private void loadProducts(Long categoryId, boolean refresh) {
        if (refresh) {
            currentCategoryId = categoryId;
            page = 1;
            hasMore = true;
            productAdapter.setData(null);
        }
        loading = true;
        final int requestPage = page;
        final Long requestCategory = categoryId;
        ApiExecutor.run(() -> DaoFactory.product().listProducts(requestCategory, null, ProductDao.SORT_DEFAULT, requestPage, PAGE_SIZE),
                new ApiCallback<PageResult<ProductVO>>() {
                    @Override
                    public void onSuccess(PageResult<ProductVO> data) {
                        loading = false;
                        // 用户可能已切换到别的分类，丢弃过期结果
                        if (!requestCategory.equals(currentCategoryId)) return;
                        if (requestPage == 1) {
                            productAdapter.setData(data.getRecords());
                        } else {
                            productAdapter.addData(data.getRecords());
                        }
                        hasMore = data.hasMore();
                        page = requestPage + 1;
                        emptyView.setVisibility(productAdapter.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onError(int code, String message) {
                        loading = false;
                        ToastUtil.show(message);
                    }
                });
    }
}
