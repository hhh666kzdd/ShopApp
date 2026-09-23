package com.example.shopapp.ui.coupon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.entity.Coupon;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.CouponVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 领券中心列表适配器（接口文档 6.1.1）：已领取的显示「已领取」并禁用按钮。
 */
public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.VH> {

    public interface OnReceiveListener {
        void onReceive(CouponVO coupon);
    }

    private final List<CouponVO> data = new ArrayList<>();
    private final OnReceiveListener listener;

    public CouponAdapter(OnReceiveListener listener) {
        this.listener = listener;
    }

    public void setData(List<CouponVO> list) {
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
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coupon, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CouponVO c = data.get(position);
        h.tvDiscount.setText(c.getDiscountText());
        h.tvType.setText(c.getType() != null && c.getType() == Coupon.TYPE_RATE ? "折扣券" : "满减券");
        h.tvName.setText(c.getName());
        h.tvDesc.setText(thresholdText(c.getThreshold())
                + "  剩余 " + safe(c.getRemain()) + "/" + safe(c.getTotal())
                + "  每人限领 " + safe(c.getPerLimit()) + " 张");
        h.tvTime.setText("有效期：" + shortDate(c.getStartTime()) + " ~ " + shortDate(c.getEndTime()));

        h.tvStatus.setVisibility(View.GONE);
        h.btnAction.setVisibility(View.VISIBLE);
        if (c.isReceived()) {
            h.btnAction.setText("已领取");
            h.btnAction.setEnabled(false);
            h.btnAction.setAlpha(0.5f);
            h.btnAction.setOnClickListener(null);
        } else {
            h.btnAction.setText("立即领取");
            h.btnAction.setEnabled(true);
            h.btnAction.setAlpha(1f);
            h.btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onReceive(c);
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private static String thresholdText(BigDecimal threshold) {
        return MoneyUtil.isPositive(threshold) ? "满 " + MoneyUtil.plain(threshold) + " 元可用" : "无门槛";
    }

    private static int safe(Integer n) {
        return n == null ? 0 : n;
    }

    /** yyyy-MM-dd HH:mm:ss 只取日期部分 */
    static String shortDate(String time) {
        if (time == null) return "-";
        return time.length() >= 10 ? time.substring(0, 10) : time;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDiscount, tvType, tvName, tvDesc, tvTime, tvStatus;
        Button btnAction;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvType = itemView.findViewById(R.id.tv_type);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}
