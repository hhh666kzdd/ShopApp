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
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.vo.UserVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台用户列表适配器：启用 / 禁用、重置密码。当前登录的管理员自己不显示启用 / 禁用按钮。
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.VH> {

    public interface Listener {
        void onToggleStatus(UserVO user);

        void onResetPassword(UserVO user);
    }

    private final List<UserVO> data = new ArrayList<>();
    private final Listener listener;

    public AdminUserAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<UserVO> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public void addData(List<UserVO> list) {
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
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserVO u = data.get(position);
        boolean enabled = u.isEnabled();
        boolean self = u.getId() != null && u.getId().equals(SessionManager.getUserId());

        ImageLoader.loadCircle(h.itemView.getContext(), u.getAvatar(), h.ivAvatar);
        h.tvUsername.setText(u.getUsername());
        h.tvRole.setVisibility(u.isAdmin() ? View.VISIBLE : View.GONE);
        h.tvStatus.setVisibility(enabled ? View.GONE : View.VISIBLE);
        h.tvInfo.setText("昵称：" + (u.getNickname() == null ? "-" : u.getNickname())
                + "　手机：" + (u.getPhone() == null || u.getPhone().isEmpty() ? "-" : u.getPhone()));
        h.tvCreateTime.setText("注册时间：" + (u.getCreateTime() == null ? "-" : u.getCreateTime()));

        h.btnToggle.setVisibility(self ? View.GONE : View.VISIBLE);
        h.btnToggle.setText(enabled ? "禁用" : "启用");
        h.btnToggle.setOnClickListener(v -> {
            if (listener != null) listener.onToggleStatus(u);
        });
        h.btnReset.setOnClickListener(v -> {
            if (listener != null) listener.onResetPassword(u);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername, tvRole, tvStatus, tvInfo, tvCreateTime;
        Button btnReset, btnToggle;

        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvInfo = itemView.findViewById(R.id.tv_info);
            tvCreateTime = itemView.findViewById(R.id.tv_create_time);
            btnReset = itemView.findViewById(R.id.btn_reset);
            btnToggle = itemView.findViewById(R.id.btn_toggle);
        }
    }
}
