package com.example.shopapp.ui.address;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.entity.Address;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;

import java.util.List;

/**
 * 收货地址列表（接口文档 7.1 / 7.5 / 7.6）：设为默认、编辑、删除、新增。
 */
public class AddressListActivity extends BaseActivity implements AddressAdapter.Listener {

    private AddressAdapter adapter;
    private View emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);
        setupToolbar("收货地址", true);

        emptyView = findViewById(R.id.empty_view);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddressAdapter(this);
        rv.setAdapter(adapter);

        findViewById(R.id.btn_add).setOnClickListener(v -> AddressEditActivity.start(this, null));
    }

    /** 新增 / 编辑返回后刷新 */
    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses();
    }

    private void loadAddresses() {
        ApiExecutor.run(() -> DaoFactory.address().listAddresses(userId()), new ApiCallback<List<Address>>() {
            @Override
            public void onSuccess(List<Address> data) {
                adapter.setData(data);
                toggleEmpty(emptyView, adapter.isEmpty());
            }

            @Override
            public void onError(int code, String message) {
                toast(message);
            }
        });
    }

    @Override
    public void onSetDefault(Address address) {
        showLoading();
        ApiExecutor.run(() -> DaoFactory.address().setDefaultAddress(userId(), address.getId()), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                hideLoading();
                loadAddresses();
            }

            @Override
            public void onError(int code, String message) {
                hideLoading();
                toast(message);
            }
        });
    }

    @Override
    public void onEdit(Address address) {
        AddressEditActivity.start(this, address);
    }

    @Override
    public void onDelete(Address address) {
        new AlertDialog.Builder(this)
                .setMessage("确定删除该收货地址？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (d, w) -> {
                    showLoading();
                    ApiExecutor.run(() -> DaoFactory.address().deleteAddress(userId(), address.getId()), new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            hideLoading();
                            toast("已删除");
                            loadAddresses();
                        }

                        @Override
                        public void onError(int code, String message) {
                            hideLoading();
                            toast(message);
                        }
                    });
                })
                .show();
    }
}
