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
import com.example.shopapp.vo.UserCouponVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的优惠券列表适配器（接口文档 6.1.3）：右侧显示状态文字，非「未使用」的条目置灰。
 */
public class MyCouponAdapter extends RecyclerView.Adapter<MyCouponAdapter.VH> {

    private final List<UserCouponVO> data = new ArrayList<>();

    public void setData(List<UserCouponVO> list) {
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
        UserCouponVO c = data.get(position);
        boolean unused = c.getStatus() != null && c.getStatus() == UserCouponVO.STATUS_UNUSED;

        h.tvDiscount.setText(c.getDiscountText());
        h.tvType.setText(c.getType() != null && c.getType() == Coupon.TYPE_RATE ? "折扣券" : "满减券");
        h.tvName.setText(c.getName());
        h.tvDesc.setText(MoneyUtil.isPositive(c.getThreshold())
                ? "满 " + MoneyUtil.plain(c.getThreshold()) + " 元可用" : "无门槛");
        h.tvTime.setText("有效期至：" + CouponAdapter.shortDate(c.getEndTime()));

        h.btnAction.setVisibility(View.GONE);
        h.tvStatus.setVisibility(View.VISIBLE);
        h.tvStatus.setText(c.getStatusText());
        h.itemView.setAlpha(unused ? 1f : 0.5f);
    }

    @Override
    public int getItemCount() {
        return data.size();
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
