package com.example.shopapp.ui.base;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView 上拉加载更多：滚动到底部时触发回调。
 * 使用方在回调中判断是否还有下一页、是否正在加载。
 */
public class LoadMoreListener extends RecyclerView.OnScrollListener {

    public interface Callback {
        void onLoadMore();
    }

    private final Callback callback;

    public LoadMoreListener(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy <= 0) return;
        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        if (!(lm instanceof LinearLayoutManager)) return; // GridLayoutManager 继承自 LinearLayoutManager
        LinearLayoutManager llm = (LinearLayoutManager) lm;
        int last = llm.findLastVisibleItemPosition();
        int total = llm.getItemCount();
        if (total > 0 && last >= total - 2) {
            callback.onLoadMore();
        }
    }
}
