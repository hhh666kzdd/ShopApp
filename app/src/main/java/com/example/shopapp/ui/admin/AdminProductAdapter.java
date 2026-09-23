package com.example.shopapp.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
 * 后台商品列表适配器：编辑、上架 / 下架、删除。
 */
public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.VH> {

    public interface Listener {
        void onEdit(ProductVO product);

        void onToggleStatus(ProductVO product);

        void onDelete(ProductVO product);
    }

    private final List<ProductVO> data = new ArrayList<>();
    private final Listener listener;

    public AdminProductAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<ProductVO> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

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
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ProductVO p = data.get(position);
        boolean onSale = p.isOnSale();

        ImageLoader.load(h.itemView.getContext(), p.getMainImage(), h.ivImage);
        h.tvName.setText(p.getName());
        h.tvCategory.setText("分类：" + (p.getCategoryName() == null ? "-" : p.getCategoryName()) + "　ID：" + p.getId());
        h.tvPrice.setText(MoneyUtil.format(p.getPrice()));
        h.tvStockSales.setText("库存 " + (p.getStock() == null ? 0 : p.getStock())
                + "　销量 " + (p.getSales() == null ? 0 : p.getSales()));
        h.tvStatus.setVisibility(onSale ? View.GONE : View.VISIBLE);
        h.btnToggle.setText(onSale ? "下架" : "上架");

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(p);
        });
        h.btnToggle.setOnClickListener(v -> {
            if (listener != null) listener.onToggleStatus(p);
        });
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvStatus, tvCategory, tvPrice, tvStockSales;
        Button btnEdit, btnToggle, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStockSales = itemView.findViewById(R.id.tv_stock_sales);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnToggle = itemView.findViewById(R.id.btn_toggle);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
