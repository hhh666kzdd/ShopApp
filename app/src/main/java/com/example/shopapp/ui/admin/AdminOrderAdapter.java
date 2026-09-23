package com.example.shopapp.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.OrderItemVO;
import com.example.shopapp.vo.OrderVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台订单列表适配器（接口文档 8.9）：待发货订单显示「发货」按钮。
 */
public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.VH> {

    public interface Listener {
        void onShip(OrderVO order);
    }

    private final List<OrderVO> data = new ArrayList<>();
    private final Listener listener;

    public AdminOrderAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<OrderVO> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public void addData(List<OrderVO> list) {
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
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OrderVO o = data.get(position);

        h.tvOrderNo.setText("订单号：" + o.getOrderNo());
        h.tvStatus.setText(o.getStatusText());
        h.tvUser.setText("用户：" + (o.getUsername() == null ? "#" + o.getUserId() : o.getUsername())
                + "　下单时间：" + (o.getCreateTime() == null ? "-" : o.getCreateTime()));
        h.tvItems.setText(buildItemsText(o.getItems()));
        h.tvReceiver.setText("收货人：" + safe(o.getReceiverName()) + "　" + safe(o.getReceiverPhone()));
        h.tvAddress.setText("地址：" + safe(o.getReceiverAddress()));
        h.tvAmount.setText("共 " + o.getTotalQuantity() + " 件　实付 " + MoneyUtil.format(o.getPayAmount()));

        boolean canShip = o.getStatus() != null && o.getStatus() == OrderVO.STATUS_PAID;
        h.btnShip.setVisibility(canShip ? View.VISIBLE : View.GONE);
        h.btnShip.setOnClickListener(v -> {
            if (listener != null) listener.onShip(o);
        });
    }

    /** 商品明细摘要：每行「名称 ×数量」 */
    private static String buildItemsText(List<OrderItemVO> items) {
        if (items == null || items.isEmpty()) return "（无商品明细）";
        StringBuilder sb = new StringBuilder();
        for (OrderItemVO item : items) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(item.getProductName()).append(" ×").append(item.getQuantity() == null ? 0 : item.getQuantity());
        }
        return sb.toString();
    }

    private static String safe(String s) {
        return s == null ? "-" : s;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvStatus, tvUser, tvItems, tvReceiver, tvAddress, tvAmount;
        Button btnShip;

        VH(@NonNull View itemView) {
            super(itemView);
            tvOrderNo = itemView.findViewById(R.id.tv_order_no);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvUser = itemView.findViewById(R.id.tv_user);
            tvItems = itemView.findViewById(R.id.tv_items);
            tvReceiver = itemView.findViewById(R.id.tv_receiver);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            btnShip = itemView.findViewById(R.id.btn_ship);
        }
    }
}
