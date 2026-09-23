package com.example.shopapp.ui.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
 * 我的订单列表适配器（接口文档 8.4）：每个订单展示明细行，并按状态显示可执行操作（8.1）。
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    public interface Listener {
        void onClick(OrderVO order);

        void onPay(OrderVO order);

        void onCancel(OrderVO order);

        void onConfirm(OrderVO order);
    }

    private final List<OrderVO> data = new ArrayList<>();
    private final Listener listener;

    public OrderAdapter(Listener listener) {
        this.listener = listener;
    }

    /** 替换全部数据 */
    public void setData(List<OrderVO> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    /** 追加一页数据（加载更多） */
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
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OrderVO order = data.get(position);
        h.tvOrderNo.setText("订单号：" + order.getOrderNo());
        h.tvStatus.setText(order.getStatusText());
        h.tvCreateTime.setText(order.getCreateTime() == null ? "" : order.getCreateTime());
        h.tvSummary.setText("共 " + order.getTotalQuantity() + " 件，实付 " + MoneyUtil.format(order.getPayAmount()));

        // 商品行：条目复用时先清空再添加
        h.layoutProducts.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(h.itemView.getContext());
        for (OrderItemVO item : order.getItems()) {
            View row = inflater.inflate(R.layout.item_order_product, h.layoutProducts, false);
            OrderItemAdapter.bind(row, item);
            h.layoutProducts.addView(row);
        }

        int status = order.getStatus() == null ? -1 : order.getStatus();
        boolean unpaid = status == OrderVO.STATUS_UNPAID;
        boolean shipped = status == OrderVO.STATUS_SHIPPED;
        h.btnCancel.setVisibility(unpaid ? View.VISIBLE : View.GONE);
        h.btnPay.setVisibility(unpaid ? View.VISIBLE : View.GONE);
        h.btnConfirm.setVisibility(shipped ? View.VISIBLE : View.GONE);
        h.layoutActions.setVisibility(unpaid || shipped ? View.VISIBLE : View.GONE);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(order);
        });
        h.btnPay.setOnClickListener(v -> {
            if (listener != null) listener.onPay(order);
        });
        h.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancel(order);
        });
        h.btnConfirm.setOnClickListener(v -> {
            if (listener != null) listener.onConfirm(order);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvStatus, tvCreateTime, tvSummary;
        LinearLayout layoutProducts, layoutActions;
        Button btnCancel, btnPay, btnConfirm;

        VH(@NonNull View itemView) {
            super(itemView);
            tvOrderNo = itemView.findViewById(R.id.tv_order_no);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCreateTime = itemView.findViewById(R.id.tv_create_time);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            layoutProducts = itemView.findViewById(R.id.layout_products);
            layoutActions = itemView.findViewById(R.id.layout_actions);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnPay = itemView.findViewById(R.id.btn_pay);
            btnConfirm = itemView.findViewById(R.id.btn_confirm);
        }
    }
}
