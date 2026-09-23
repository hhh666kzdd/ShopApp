package com.example.shopapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.ui.address.AddressListActivity;
import com.example.shopapp.ui.admin.AdminMainActivity;
import com.example.shopapp.ui.base.ImageLoader;
import com.example.shopapp.ui.coupon.CouponCenterActivity;
import com.example.shopapp.ui.coupon.MyCouponActivity;
import com.example.shopapp.ui.order.OrderListActivity;
import com.example.shopapp.ui.user.ChangePasswordActivity;
import com.example.shopapp.ui.user.LoginActivity;
import com.example.shopapp.ui.user.ProfileEditActivity;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.vo.OrderVO;

/**
 * 我的页面：个人信息、订单入口、地址、优惠券、资料、密码、后台管理、退出登录。
 */
public class MineFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvNickname, tvUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvNickname = view.findViewById(R.id.tv_nickname);
        tvUsername = view.findViewById(R.id.tv_username);

        view.findViewById(R.id.layout_header).setOnClickListener(v -> open(ProfileEditActivity.class));

        // 订单入口
        view.findViewById(R.id.item_all_orders).setOnClickListener(v -> OrderListActivity.start(requireContext(), null));
        view.findViewById(R.id.tv_order_unpaid).setOnClickListener(v -> OrderListActivity.start(requireContext(), OrderVO.STATUS_UNPAID));
        view.findViewById(R.id.tv_order_paid).setOnClickListener(v -> OrderListActivity.start(requireContext(), OrderVO.STATUS_PAID));
        view.findViewById(R.id.tv_order_shipped).setOnClickListener(v -> OrderListActivity.start(requireContext(), OrderVO.STATUS_SHIPPED));
        view.findViewById(R.id.tv_order_finished).setOnClickListener(v -> OrderListActivity.start(requireContext(), OrderVO.STATUS_FINISHED));

        // 功能入口
        setupEntry(view, R.id.item_address, R.drawable.ic_location, "收货地址", v -> open(AddressListActivity.class));
        setupEntry(view, R.id.item_coupon_center, R.drawable.ic_coupon, "领券中心", v -> open(CouponCenterActivity.class));
        setupEntry(view, R.id.item_my_coupon, R.drawable.ic_order, "我的优惠券", v -> open(MyCouponActivity.class));
        setupEntry(view, R.id.item_profile, R.drawable.ic_edit, "个人资料", v -> open(ProfileEditActivity.class));
        setupEntry(view, R.id.item_password, R.drawable.ic_lock, "修改密码", v -> open(ChangePasswordActivity.class));
        View admin = setupEntry(view, R.id.item_admin, R.drawable.ic_admin, "后台管理", v -> open(AdminMainActivity.class));
        admin.setVisibility(SessionManager.isAdmin() ? View.VISIBLE : View.GONE);
        setupEntry(view, R.id.item_logout, R.drawable.ic_logout, "退出登录", v -> logout());
    }

    @Override
    public void onResume() {
        super.onResume();
        renderUser();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) renderUser();
    }

    /** 从本地会话读取昵称、头像（修改资料后 SessionManager 会同步更新） */
    private void renderUser() {
        tvNickname.setText(SessionManager.getNickname());
        tvUsername.setText("账号：" + SessionManager.getUsername());
        ImageLoader.loadCircle(requireContext(), SessionManager.getAvatar(), ivAvatar);
    }

    private View setupEntry(View root, int id, int iconRes, String title, View.OnClickListener listener) {
        View entry = root.findViewById(id);
        ((ImageView) entry.findViewById(R.id.iv_icon)).setImageResource(iconRes);
        ((TextView) entry.findViewById(R.id.tv_title)).setText(title);
        entry.setOnClickListener(listener);
        return entry;
    }

    private void open(Class<?> activity) {
        startActivity(new Intent(requireContext(), activity));
    }

    /** 退出登录（接口文档 2.6）：清除本地会话后回到登录页 */
    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setMessage("确定退出登录？")
                .setNegativeButton("取消", null)
                .setPositiveButton("退出", (d, w) -> {
                    DaoFactory.user().logout();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .show();
    }
}
