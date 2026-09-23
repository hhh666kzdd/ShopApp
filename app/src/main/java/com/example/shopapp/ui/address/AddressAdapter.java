package com.example.shopapp.ui.address;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.entity.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * 收货地址列表适配器：默认地址显示「默认」标签并隐藏「设为默认」。
 */
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.VH> {

    public interface Listener {
        void onSetDefault(Address address);

        void onEdit(Address address);

        void onDelete(Address address);
    }

    private final List<Address> data = new ArrayList<>();
    private final Listener listener;

    public AddressAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<Address> list) {
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
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Address a = data.get(position);
        boolean isDefault = a.isDefaultAddress();
        h.tvName.setText(a.getReceiverName());
        h.tvPhone.setText(a.getReceiverPhone());
        h.tvAddress.setText(a.getFullAddress());
        h.tvDefault.setVisibility(isDefault ? View.VISIBLE : View.GONE);
        h.tvSetDefault.setVisibility(isDefault ? View.GONE : View.VISIBLE);

        h.tvSetDefault.setOnClickListener(v -> {
            if (listener != null) listener.onSetDefault(a);
        });
        h.tvEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(a);
        });
        h.tvDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(a);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvDefault, tvAddress, tvSetDefault, tvEdit, tvDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvDefault = itemView.findViewById(R.id.tv_default);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvSetDefault = itemView.findViewById(R.id.tv_set_default);
            tvEdit = itemView.findViewById(R.id.tv_edit);
            tvDelete = itemView.findViewById(R.id.tv_delete);
        }
    }
}
