package com.example.shopapp.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.product.ProductAdapter;
import com.example.shopapp.ui.product.ProductDetailActivity;
import com.example.shopapp.ui.product.ProductListActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.ToastUtil;
import com.example.shopapp.vo.ProductVO;
import com.example.shopapp.vo.PromotionVO;

import java.util.List;

/**
 * 首页：搜索、限时特价（6.2.1）、热销推荐（4.3）。
 */
public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private View layoutPromotion;
    private PromotionAdapter promotionAdapter;
    private ProductAdapter hotAdapter;
    private EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        layoutPromotion = view.findViewById(R.id.layout_promotion);
        etSearch = view.findViewById(R.id.et_search);

        RecyclerView rvPromotion = view.findViewById(R.id.rv_promotion);
        rvPromotion.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        promotionAdapter = new PromotionAdapter(p -> ProductDetailActivity.start(requireContext(), p.getProductId()));
        rvPromotion.setAdapter(promotionAdapter);

        RecyclerView rvHot = view.findViewById(R.id.rv_hot);
        rvHot.setLayoutManager(new GridLayoutManager(getContext(), 2));
        hotAdapter = new ProductAdapter(p -> ProductDetailActivity.start(requireContext(), p.getId()));
        rvHot.setAdapter(hotAdapter);

        // 搜索：回车或点击「搜索」
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search();
                return true;
            }
            return false;
        });
        view.findViewById(R.id.tv_search).setOnClickListener(v -> search());

        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    private void search() {
        String keyword = etSearch.getText().toString().trim();
        ProductListActivity.start(requireContext(), null, keyword, keyword.isEmpty() ? "全部商品" : "搜索：" + keyword);
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        // 限时特价
        ApiExecutor.run(() -> DaoFactory.promotion().listActivePromotions(), new ApiCallback<List<PromotionVO>>() {
            @Override
            public void onSuccess(List<PromotionVO> data) {
                promotionAdapter.setData(data);
                layoutPromotion.setVisibility(data == null || data.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(int code, String message) {
                layoutPromotion.setVisibility(View.GONE);
            }
        });
        // 热销推荐
        ApiExecutor.run(() -> DaoFactory.product().listHotProducts(20), new ApiCallback<List<ProductVO>>() {
            @Override
            public void onSuccess(List<ProductVO> data) {
                swipeRefresh.setRefreshing(false);
                hotAdapter.setData(data);
            }

            @Override
            public void onError(int code, String message) {
                swipeRefresh.setRefreshing(false);
                ToastUtil.show(message);
            }
        });
    }
}
