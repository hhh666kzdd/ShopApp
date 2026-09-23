package com.example.shopapp.ui.admin;

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
 * 后台限时特价活动列表适配器（接口文档 6.2.3）。
 */
public class AdminPromotionAdapter extends RecyclerView.Adapter<AdminPromotionAdapter.VH> {

    public interface Listener {
        void onEdit(PromotionVO promotion);

        void onDelete(PromotionVO promotion);
    }

    private final List<PromotionVO> data = new ArrayList<>();
    private final Listener listener;

    public AdminPromotionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<PromotionVO> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public void addData(List<PromotionVO> list) {
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
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_promotion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PromotionVO p = data.get(position);
        boolean enabled = p.getStatus() != null && p.getStatus() == 1;

        ImageLoader.load(h.itemView.getContext(), p.getProductImage(), h.ivImage);
        h.tvTitle.setText(p.getTitle());
        h.tvProduct.setText("商品：" + (p.getProductName() == null ? "#" + p.getProductId() : p.getProductName()));
        h.tvPromoPrice.setText(MoneyUtil.format(p.getPromoPrice()));
        h.tvOriginalPrice.setText(MoneyUtil.format(p.getOriginalPrice()));
        h.tvOriginalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        h.tvTime.setText((p.getStartTime() == null ? "-" : p.getStartTime()) + " ~ " + (p.getEndTime() == null ? "-" : p.getEndTime()));
        h.tvStatus.setText(enabled ? "启用" : "停用");
        h.tvStatus.setBackgroundResource(enabled ? R.drawable.bg_tag_promo : R.drawable.bg_tag_gray);

        h.tvEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(p);
        });
        h.tvDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvStatus, tvProduct, tvPromoPrice, tvOriginalPrice, tvTime, tvEdit, tvDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvProduct = itemView.findViewById(R.id.tv_product);
            tvPromoPrice = itemView.findViewById(R.id.tv_promo_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvEdit = itemView.findViewById(R.id.tv_edit);
            tvDelete = itemView.findViewById(R.id.tv_delete);
        }
    }
}
