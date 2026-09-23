package com.example.shopapp.dao.impl;

import com.example.shopapp.common.BizException;
import com.example.shopapp.common.PageResult;
import com.example.shopapp.common.Result;
import com.example.shopapp.common.ResultCode;
import com.example.shopapp.dao.AdminDao;
import com.example.shopapp.util.MD5Util;
import com.example.shopapp.vo.StatisticsVO;
import com.example.shopapp.vo.UserVO;

/**
 * 后台管理模块实现（接口文档 9）。
 */
public class AdminDaoImpl extends BaseDao implements AdminDao {

    /** 重置密码使用的默认密码 */
    public static final String DEFAULT_PASSWORD = "123456";

    @Override
    public Result<PageResult<UserVO>> listUsers(Long adminId, String keyword, int page, int size) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            String kw = like(keyword);
            String where = kw == null ? "" : " WHERE username LIKE ? OR nickname LIKE ? OR phone LIKE ?";
            Object[] params = kw == null ? new Object[0] : new Object[]{kw, kw, kw};
            return queryPage(conn, "SELECT COUNT(*) FROM t_user" + where,
                    "SELECT * FROM t_user" + where + " ORDER BY id DESC",
                    UserDaoImpl::mapUser, page, size, params);
        });
    }

    @Override
    public Result<Void> updateUserStatus(Long adminId, Long userId, Integer status) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            requireNotNull(userId, "用户 ID");
            if (status == null || (status != 0 && status != 1)) {
                throw new BizException(ResultCode.BAD_REQUEST, "状态值不正确");
            }
            if (userId.equals(adminId) && status == 0) {
                throw new BizException(ResultCode.BAD_REQUEST, "不能禁用管理员自己");
            }
            int rows = update(conn, "UPDATE t_user SET status = ?, update_time = NOW() WHERE id = ?", status, userId);
            if (rows == 0) throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
            return null;
        });
    }

    @Override
    public Result<Void> resetPassword(Long adminId, Long userId) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            requireNotNull(userId, "用户 ID");
            int rows = update(conn, "UPDATE t_user SET password = ?, update_time = NOW() WHERE id = ?",
                    MD5Util.md5(DEFAULT_PASSWORD), userId);
            if (rows == 0) throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
            return null;
        });
    }

    @Override
    public Result<StatisticsVO> getStatistics(Long adminId) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            StatisticsVO vo = new StatisticsVO();
            vo.setUserCount((int) queryLong(conn, "SELECT COUNT(*) FROM t_user WHERE role = 0"));
            vo.setProductCount((int) queryLong(conn, "SELECT COUNT(*) FROM t_product WHERE status = 1"));
            // 累计：已支付的有效订单（待发货、待收货、已完成）
            vo.setOrderCount((int) queryLong(conn, "SELECT COUNT(*) FROM t_order WHERE status IN (1,2,3)"));
            vo.setTotalSales(queryDecimal(conn, "SELECT IFNULL(SUM(pay_amount),0) FROM t_order WHERE status IN (1,2,3)"));
            vo.setTodayOrderCount((int) queryLong(conn,
                    "SELECT COUNT(*) FROM t_order WHERE status IN (1,2,3) AND DATE(create_time) = CURDATE()"));
            vo.setTodaySales(queryDecimal(conn,
                    "SELECT IFNULL(SUM(pay_amount),0) FROM t_order WHERE status IN (1,2,3) AND DATE(create_time) = CURDATE()"));
            vo.setPendingShipCount((int) queryLong(conn, "SELECT COUNT(*) FROM t_order WHERE status = 1"));
            return vo;
        });
    }
}
