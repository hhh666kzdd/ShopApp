package com.example.shopapp.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.entity.Category;
import com.example.shopapp.ui.base.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台分类列表适配器：编辑、删除。
 */
public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.VH> {

    public interface Listener {
        void onEdit(Category category);

        void onDelete(Category category);
    }

    private final List<Category> data = new ArrayList<>();
    private final Listener listener;

    public AdminCategoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<Category> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Category c = data.get(position);
        boolean enabled = c.getStatus() == null || c.getStatus() == 1;

        ImageLoader.load(h.itemView.getContext(), c.getIcon(), h.ivIcon);
        h.tvName.setText(c.getName());
        h.tvSort.setText("排序：" + (c.getSort() == null ? 0 : c.getSort()) + "　ID：" + c.getId());
        h.tvStatus.setVisibility(enabled ? View.GONE : View.VISIBLE);
        h.itemView.setAlpha(enabled ? 1f : 0.6f);

        h.tvEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(c);
        });
        h.tvDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(c);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvStatus, tvSort, tvEdit, tvDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvSort = itemView.findViewById(R.id.tv_sort);
            tvEdit = itemView.findViewById(R.id.tv_edit);
            tvDelete = itemView.findViewById(R.id.tv_delete);
        }
    }
}
