package com.example.shopapp.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.vo.CouponVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台优惠券列表适配器（接口文档 6.1.6）。
 */
public class AdminCouponAdapter extends RecyclerView.Adapter<AdminCouponAdapter.VH> {

    public interface Listener {
        void onEdit(CouponVO coupon);

        void onToggleStatus(CouponVO coupon);

        void onDelete(CouponVO coupon);
    }

    private final List<CouponVO> data = new ArrayList<>();
    private final Listener listener;

    public AdminCouponAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<CouponVO> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public void addData(List<CouponVO> list) {
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
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_coupon, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CouponVO c = data.get(position);
        boolean enabled = c.getStatus() != null && c.getStatus() == 1;

        h.tvName.setText(c.getName());
        h.tvDiscount.setText(c.getDiscountText());
        h.tvQuantity.setText("剩余 " + (c.getRemain() == null ? 0 : c.getRemain())
                + " / 总量 " + (c.getTotal() == null ? 0 : c.getTotal())
                + "　每人限领 " + (c.getPerLimit() == null ? 1 : c.getPerLimit()) + " 张");
        h.tvTime.setText("有效期：" + safe(c.getStartTime()) + " ~ " + safe(c.getEndTime()));
        h.tvStatus.setText(enabled ? "启用" : "停用");
        h.tvStatus.setBackgroundResource(enabled ? R.drawable.bg_tag_promo : R.drawable.bg_tag_gray);
        h.tvToggle.setText(enabled ? "停用" : "启用");

        h.tvToggle.setOnClickListener(v -> {
            if (listener != null) listener.onToggleStatus(c);
        });
        h.tvEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(c);
        });
        h.tvDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(c);
        });
    }

    private static String safe(String s) {
        return s == null ? "-" : s;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvDiscount, tvQuantity, tvTime, tvToggle, tvEdit, tvDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvToggle = itemView.findViewById(R.id.tv_toggle);
            tvEdit = itemView.findViewById(R.id.tv_edit);
            tvDelete = itemView.findViewById(R.id.tv_delete);
        }
    }
}
