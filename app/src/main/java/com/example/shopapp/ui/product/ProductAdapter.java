package com.example.shopapp.ui.product;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.ui.base.ImageLoader;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.ProductVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品网格适配器：首页热销、分类页、搜索列表共用。
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnItemClickListener {
        void onClick(ProductVO product);
    }

    private final List<ProductVO> data = new ArrayList<>();
    private final OnItemClickListener listener;

    public ProductAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /** 替换全部数据 */
    public void setData(List<ProductVO> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    /** 追加一页数据（加载更多） */
    public void addData(List<ProductVO> list) {
        if (list == null || list.isEmpty()) return;
        int start = data.size();
        data.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ProductVO p = data.get(position);
        ImageLoader.load(h.itemView.getContext(), p.getMainImage(), h.ivImage);
        h.tvName.setText(p.getName());
        h.tvPrice.setText(MoneyUtil.format(p.getCurrentPrice()));
        h.tvSales.setText("已售 " + (p.getSales() == null ? 0 : p.getSales()));
        if (p.hasPromotion()) {
            // 有活动：显示划线原价和标签
            h.tvOriginalPrice.setVisibility(View.VISIBLE);
            h.tvOriginalPrice.setText(MoneyUtil.format(p.getPrice()));
            h.tvOriginalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            h.tvPromoTag.setVisibility(View.VISIBLE);
        } else {
            h.tvOriginalPrice.setVisibility(View.GONE);
            h.tvPromoTag.setVisibility(View.GONE);
        }
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvOriginalPrice, tvPromoTag, tvSales;

        VH(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvPromoTag = itemView.findViewById(R.id.tv_promo_tag);
            tvSales = itemView.findViewById(R.id.tv_sales);
        }
    }
}
