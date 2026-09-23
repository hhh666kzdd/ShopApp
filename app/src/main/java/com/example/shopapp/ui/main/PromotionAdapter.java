package com.example.shopapp.ui.main;

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
import com.example.shopapp.vo.PromotionVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页「限时特价」横向列表适配器。
 */
public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.VH> {

    public interface OnItemClickListener {
        void onClick(PromotionVO promotion);
    }

    private final List<PromotionVO> data = new ArrayList<>();
    private final OnItemClickListener listener;

    public PromotionAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<PromotionVO> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PromotionVO p = data.get(position);
        ImageLoader.load(h.itemView.getContext(), p.getProductImage(), h.ivImage);
        h.tvName.setText(p.getProductName());
        h.tvPromoPrice.setText(MoneyUtil.format(p.getPromoPrice()));
        h.tvOriginalPrice.setText(MoneyUtil.format(p.getOriginalPrice()));
        h.tvOriginalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        h.tvTitle.setText(p.getTitle());
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
        TextView tvName, tvPromoPrice, tvOriginalPrice, tvTitle;

        VH(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPromoPrice = itemView.findViewById(R.id.tv_promo_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}
