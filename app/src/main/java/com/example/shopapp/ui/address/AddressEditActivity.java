package com.example.shopapp.ui.address;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.entity.Address;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.ValidateUtil;

/**
 * 新增 / 编辑收货地址（接口文档 7.3 / 7.4）。
 */
public class AddressEditActivity extends BaseActivity {

    private static final String EXTRA_ADDRESS = "address";

    private EditText etName, etPhone, etProvince, etCity, etDistrict, etDetail;
    private CheckBox cbDefault;

    /** 编辑中的地址，为 null 表示新增 */
    private Address editing;

    /**
     * @param address 要编辑的地址；传 null 表示新增
     */
    public static void start(Context context, Address address) {
        Intent intent = new Intent(context, AddressEditActivity.class);
        if (address != null) intent.putExtra(EXTRA_ADDRESS, address);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_edit);
        editing = (Address) getIntent().getSerializableExtra(EXTRA_ADDRESS);
        setupToolbar(editing == null ? "新增地址" : "编辑地址", true);

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etProvince = findViewById(R.id.et_province);
        etCity = findViewById(R.id.et_city);
        etDistrict = findViewById(R.id.et_district);
        etDetail = findViewById(R.id.et_detail);
        cbDefault = findViewById(R.id.cb_default);
        findViewById(R.id.btn_save).setOnClickListener(v -> save());

        if (editing != null) {
            etName.setText(editing.getReceiverName());
            etPhone.setText(editing.getReceiverPhone());
            etProvince.setText(editing.getProvince());
            etCity.setText(editing.getCity());
            etDistrict.setText(editing.getDistrict());
            etDetail.setText(editing.getDetail());
            cbDefault.setChecked(editing.isDefaultAddress());
        }
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String province = etProvince.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String detail = etDetail.getText().toString().trim();

        if (name.isEmpty()) {
            toast("请输入收货人");
            return;
        }
        if (!ValidateUtil.isPhone(phone)) {
            toast("手机号格式不正确");
            return;
        }
        if (province.isEmpty() || city.isEmpty() || district.isEmpty()) {
            toast("请填写省、市、区/县");
            return;
        }
        if (detail.isEmpty()) {
            toast("请输入详细地址");
            return;
        }

        Address address = new Address();
        if (editing != null) address.setId(editing.getId());
        address.setUserId(userId());
        address.setReceiverName(name);
        address.setReceiverPhone(phone);
        address.setProvince(province);
        address.setCity(city);
        address.setDistrict(district);
        address.setDetail(detail);
        address.setIsDefault(cbDefault.isChecked() ? 1 : 0);

        showLoading("保存中…");
        ApiExecutor.run(() -> editing == null
                        ? DaoFactory.address().addAddress(userId(), address)
                        : DaoFactory.address().updateAddress(userId(), address),
                new ApiCallback<Address>() {
                    @Override
                    public void onSuccess(Address data) {
                        hideLoading();
                        toast("保存成功");
                        finish();
                    }

                    @Override
                    public void onError(int code, String message) {
                        hideLoading();
                        toast(message);
                    }
                });
    }
}
