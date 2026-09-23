package com.example.shopapp.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.ui.base.ImageLoader;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.CartItemVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车列表适配器。已下架商品置灰且不可勾选。
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public interface Listener {
        void onCheckedChange(CartItemVO item, boolean checked);

        void onQuantityChange(CartItemVO item, int newQuantity);

        void onDelete(CartItemVO item);

        void onItemClick(CartItemVO item);
    }

    private final List<CartItemVO> data = new ArrayList<>();
    private final Listener listener;

    public CartAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<CartItemVO> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public List<CartItemVO> getData() {
        return data;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CartItemVO item = data.get(position);
        boolean available = item.getStatus() != null && item.getStatus() == 1;

        ImageLoader.load(h.itemView.getContext(), item.getProductImage(), h.ivImage);
        h.tvName.setText(item.getProductName());
        h.tvPrice.setText(MoneyUtil.format(item.getCurrentPrice()));
        h.tvQuantity.setText(String.valueOf(item.getQuantity()));
        h.tvStatus.setVisibility(available ? View.GONE : View.VISIBLE);
        h.itemView.setAlpha(available ? 1f : 0.5f);

        // 先移除监听再设置状态，避免复用时误触发
        h.cbChecked.setOnCheckedChangeListener(null);
        h.cbChecked.setChecked(available && item.isChecked());
        h.cbChecked.setEnabled(available);
        h.cbChecked.setOnCheckedChangeListener((btn, checked) -> {
            if (listener != null) listener.onCheckedChange(item, checked);
        });

        h.ivMinus.setOnClickListener(v -> {
            if (!available || listener == null) return;
            int q = item.getQuantity() - 1;
            if (q >= 1) listener.onQuantityChange(item, q);
        });
        h.ivPlus.setOnClickListener(v -> {
            if (!available || listener == null) return;
            listener.onQuantityChange(item, item.getQuantity() + 1);
        });
        h.tvDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
        h.ivImage.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cbChecked;
        ImageView ivImage, ivMinus, ivPlus;
        TextView tvName, tvStatus, tvPrice, tvQuantity, tvDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            cbChecked = itemView.findViewById(R.id.cb_checked);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivMinus = itemView.findViewById(R.id.iv_minus);
            ivPlus = itemView.findViewById(R.id.iv_plus);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvDelete = itemView.findViewById(R.id.tv_delete);
        }
    }
}
