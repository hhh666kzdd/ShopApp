package com.example.shopapp.ui.order;

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
import com.example.shopapp.vo.OrderItemVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单商品明细适配器（接口文档 8.11 OrderItemVO）：结算页与订单详情页共用。
 */
public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.VH> {

    private final List<OrderItemVO> data = new ArrayList<>();

    public void setData(List<OrderItemVO> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    /** 把一条明细绑定到 item_order_product 布局，订单列表中动态添加的行也使用此方法 */
    public static void bind(View row, OrderItemVO item) {
        ImageView ivImage = row.findViewById(R.id.iv_image);
        TextView tvName = row.findViewById(R.id.tv_name);
        TextView tvPriceQuantity = row.findViewById(R.id.tv_price_quantity);
        TextView tvSubtotal = row.findViewById(R.id.tv_subtotal);

        ImageLoader.load(row.getContext(), item.getProductImage(), ivImage);
        tvName.setText(item.getProductName());
        int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
        tvPriceQuantity.setText(MoneyUtil.format(item.getPrice()) + " × " + quantity);
        tvSubtotal.setText(MoneyUtil.format(item.getTotalPrice()));
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_product, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        bind(h.itemView, data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        VH(@NonNull View itemView) {
            super(itemView);
        }
    }
}
