package com.example.shopapp.dao.impl;

import com.example.shopapp.common.BizException;
import com.example.shopapp.common.Result;
import com.example.shopapp.common.ResultCode;
import com.example.shopapp.dao.AddressDao;
import com.example.shopapp.entity.Address;
import com.example.shopapp.util.ValidateUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 收货地址模块实现（接口文档 7）。
 */
public class AddressDaoImpl extends BaseDao implements AddressDao {

    static Address mapAddress(ResultSet rs) throws SQLException {
        Address a = new Address();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setReceiverName(rs.getString("receiver_name"));
        a.setReceiverPhone(rs.getString("receiver_phone"));
        a.setProvince(rs.getString("province"));
        a.setCity(rs.getString("city"));
        a.setDistrict(rs.getString("district"));
        a.setDetail(rs.getString("detail"));
        a.setIsDefault(getInt(rs, "is_default"));
        return a;
    }

    /** 供订单模块复用：查询属于该用户的地址，不存在返回 null */
    static Address findByIdAndUser(Connection conn, Long userId, Long id) throws SQLException {
        return new AddressDaoImpl().queryOne(conn, "SELECT * FROM t_address WHERE id = ? AND user_id = ?",
                AddressDaoImpl::mapAddress, id, userId);
    }

    /** 供订单模块复用：默认地址，不存在返回 null */
    static Address findDefault(Connection conn, Long userId) throws SQLException {
        return new AddressDaoImpl().queryOne(conn,
                "SELECT * FROM t_address WHERE user_id = ? AND is_default = 1 ORDER BY id DESC LIMIT 1",
                AddressDaoImpl::mapAddress, userId);
    }

    /** 必填字段校验 */
    private void validate(Address a) {
        requireNotNull(a, "地址");
        requireNotBlank(a.getReceiverName(), "收货人");
        if (!ValidateUtil.isPhone(a.getReceiverPhone())) {
            throw new BizException(ResultCode.BAD_REQUEST, "收货人手机号格式不正确");
        }
        requireNotBlank(a.getProvince(), "省");
        requireNotBlank(a.getCity(), "市");
        requireNotBlank(a.getDistrict(), "区 / 县");
        requireNotBlank(a.getDetail(), "详细地址");
    }

    @Override
    public Result<List<Address>> listAddresses(Long userId) {
        return execute(conn -> {
            requireLogin(userId);
            return query(conn, "SELECT * FROM t_address WHERE user_id = ? ORDER BY is_default DESC, id DESC",
                    AddressDaoImpl::mapAddress, userId);
        });
    }

    @Override
    public Result<Address> getAddress(Long userId, Long id) {
        return execute(conn -> {
            requireLogin(userId);
            requireNotNull(id, "地址 ID");
            Address a = findByIdAndUser(conn, userId, id);
            if (a == null) throw new BizException(ResultCode.ADDRESS_NOT_FOUND);
            return a;
        });
    }

    @Override
    public Result<Address> addAddress(Long userId, Address address) {
        return executeTx(conn -> {
            requireLogin(userId);
            validate(address);
            long count = queryLong(conn, "SELECT COUNT(*) FROM t_address WHERE user_id = ?", userId);
            // 首个地址或指定默认时设为默认
            boolean makeDefault = count == 0 || address.isDefaultAddress();
            if (makeDefault) {
                update(conn, "UPDATE t_address SET is_default = 0 WHERE user_id = ?", userId);
            }
            long id = insert(conn,
                    "INSERT INTO t_address(user_id, receiver_name, receiver_phone, province, city, district, detail, is_default, create_time) "
                            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                    userId, address.getReceiverName().trim(), address.getReceiverPhone().trim(),
                    address.getProvince().trim(), address.getCity().trim(), address.getDistrict().trim(),
                    address.getDetail().trim(), makeDefault ? 1 : 0);
            return findByIdAndUser(conn, userId, id);
        });
    }

    @Override
    public Result<Address> updateAddress(Long userId, Address address) {
        return executeTx(conn -> {
            requireLogin(userId);
            validate(address);
            requireNotNull(address.getId(), "地址 ID");
            Address old = findByIdAndUser(conn, userId, address.getId());
            if (old == null) throw new BizException(ResultCode.ADDRESS_NOT_FOUND);
            boolean makeDefault = address.isDefaultAddress();
            if (makeDefault) {
                update(conn, "UPDATE t_address SET is_default = 0 WHERE user_id = ?", userId);
            }
            // 未勾选默认时保持原来的默认状态，避免用户唯一的默认地址被取消
            int isDefault = makeDefault ? 1 : old.getIsDefault();
            update(conn,
                    "UPDATE t_address SET receiver_name=?, receiver_phone=?, province=?, city=?, district=?, detail=?, is_default=? "
                            + "WHERE id=? AND user_id=?",
                    address.getReceiverName().trim(), address.getReceiverPhone().trim(), address.getProvince().trim(),
                    address.getCity().trim(), address.getDistrict().trim(), address.getDetail().trim(), isDefault,
                    address.getId(), userId);
            return findByIdAndUser(conn, userId, address.getId());
        });
    }

    @Override
    public Result<Void> deleteAddress(Long userId, Long id) {
        return executeTx(conn -> {
            requireLogin(userId);
            requireNotNull(id, "地址 ID");
            Address old = findByIdAndUser(conn, userId, id);
            if (old == null) throw new BizException(ResultCode.ADDRESS_NOT_FOUND);
            update(conn, "DELETE FROM t_address WHERE id = ? AND user_id = ?", id, userId);
            // 删除的是默认地址时，把最新的一条地址设为默认
            if (old.isDefaultAddress()) {
                update(conn, "UPDATE t_address SET is_default = 1 WHERE user_id = ? ORDER BY id DESC LIMIT 1", userId);
            }
            return null;
        });
    }

    @Override
    public Result<Void> setDefaultAddress(Long userId, Long id) {
        return executeTx(conn -> {
            requireLogin(userId);
            requireNotNull(id, "地址 ID");
            if (findByIdAndUser(conn, userId, id) == null) throw new BizException(ResultCode.ADDRESS_NOT_FOUND);
            update(conn, "UPDATE t_address SET is_default = 0 WHERE user_id = ?", userId);
            update(conn, "UPDATE t_address SET is_default = 1 WHERE id = ? AND user_id = ?", id, userId);
            return null;
        });
    }

    @Override
    public Result<Address> getDefaultAddress(Long userId) {
        return execute(conn -> {
            requireLogin(userId);
            // 无默认地址时 data 为 null
            return findDefault(conn, userId);
        });
    }
}
