package com.example.shopapp.ui.main;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.entity.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类页左侧分类列表适配器，高亮当前选中项。
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public interface OnItemClickListener {
        void onClick(Category category, int position);
    }

    private final List<Category> data = new ArrayList<>();
    private final OnItemClickListener listener;
    private int selected = 0;

    public CategoryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Category> list) {
        data.clear();
        if (list != null) data.addAll(list);
        selected = 0;
        notifyDataSetChanged();
    }

    public Category getItem(int position) {
        return position >= 0 && position < data.size() ? data.get(position) : null;
    }

    public void setSelected(int position) {
        int old = selected;
        selected = position;
        notifyItemChanged(old);
        notifyItemChanged(selected);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Category c = data.get(position);
        boolean isSelected = position == selected;
        h.tvName.setText(c.getName());
        h.tvName.setBackgroundColor(ContextCompat.getColor(h.itemView.getContext(),
                isSelected ? R.color.white : R.color.background));
        h.tvName.setTextColor(ContextCompat.getColor(h.itemView.getContext(),
                isSelected ? R.color.primary : R.color.text_secondary));
        h.tvName.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
        h.itemView.setOnClickListener(v -> {
            setSelected(position);
            if (listener != null) listener.onClick(c, position);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}
