package com.example.shopapp.dao;

import com.example.shopapp.common.Result;
import com.example.shopapp.entity.Address;

import java.util.List;

/**
 * 收货地址模块（接口文档 7）。
 */
public interface AddressDao {

    /** 7.1 地址列表：默认地址排最前 */
    Result<List<Address>> listAddresses(Long userId);

    /** 7.2 地址详情。失败码 5001 */
    Result<Address> getAddress(Long userId, Long id);

    /**
     * 7.3 新增地址：收货人、手机、省市区、详细地址必填；
     * 若 isDefault=1 或用户尚无地址，则设为默认（其它地址取消默认）。
     */
    Result<Address> addAddress(Long userId, Address address);

    /** 7.4 修改地址，address.id 必填 */
    Result<Address> updateAddress(Long userId, Address address);

    /** 7.5 删除地址 */
    Result<Void> deleteAddress(Long userId, Long id);

    /** 7.6 设为默认地址 */
    Result<Void> setDefaultAddress(Long userId, Long id);

    /** 7.7 获取默认地址：无默认地址时 data=null */
    Result<Address> getDefaultAddress(Long userId);
}
