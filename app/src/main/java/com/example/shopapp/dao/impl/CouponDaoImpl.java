package com.example.shopapp.dao.impl;

import com.example.shopapp.common.BizException;
import com.example.shopapp.common.PageResult;
import com.example.shopapp.common.Result;
import com.example.shopapp.common.ResultCode;
import com.example.shopapp.dao.CouponDao;
import com.example.shopapp.entity.Coupon;
import com.example.shopapp.util.DateUtil;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.CouponVO;
import com.example.shopapp.vo.UserCouponVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 优惠券模块实现（接口文档 6.1）。
 */
public class CouponDaoImpl extends BaseDao implements CouponDao {

    /** 用户优惠券联查优惠券定义 */
    static final String SELECT_USER_COUPON =
            "SELECT uc.id, uc.coupon_id, uc.status, uc.receive_time, uc.use_time, "
                    + "c.name, c.type, c.threshold, c.discount_amount, c.discount_rate, c.end_time "
                    + "FROM t_user_coupon uc JOIN t_coupon c ON uc.coupon_id = c.id ";

    static CouponVO mapCoupon(ResultSet rs) throws SQLException {
        CouponVO vo = new CouponVO();
        vo.setId(rs.getLong("id"));
        vo.setName(rs.getString("name"));
        vo.setType(getInt(rs, "type"));
        vo.setThreshold(rs.getBigDecimal("threshold"));
        vo.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        vo.setDiscountRate(rs.getBigDecimal("discount_rate"));
        vo.setTotal(getInt(rs, "total"));
        vo.setRemain(getInt(rs, "remain"));
        vo.setPerLimit(getInt(rs, "per_limit"));
        vo.setStartTime(getTime(rs, "start_time"));
        vo.setEndTime(getTime(rs, "end_time"));
        vo.setStatus(getInt(rs, "status"));
        return vo;
    }

    static UserCouponVO mapUserCoupon(ResultSet rs) throws SQLException {
        UserCouponVO vo = new UserCouponVO();
        vo.setId(rs.getLong("id"));
        vo.setCouponId(rs.getLong("coupon_id"));
        vo.setName(rs.getString("name"));
        vo.setType(getInt(rs, "type"));
        vo.setThreshold(rs.getBigDecimal("threshold"));
        vo.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        vo.setDiscountRate(rs.getBigDecimal("discount_rate"));
        vo.setStatus(getInt(rs, "status"));
        vo.setReceiveTime(getTime(rs, "receive_time"));
        vo.setUseTime(getTime(rs, "use_time"));
        vo.setEndTime(getTime(rs, "end_time"));
        return vo;
    }

    private CouponVO findById(Connection conn, Long id) throws SQLException {
        return queryOne(conn, "SELECT * FROM t_coupon WHERE id = ?", CouponDaoImpl::mapCoupon, id);
    }

    /** 供订单模块复用：查询属于该用户的用户优惠券，不存在返回 null */
    static UserCouponVO findUserCoupon(Connection conn, Long userId, Long userCouponId) throws SQLException {
        return new CouponDaoImpl().queryOne(conn, SELECT_USER_COUPON + "WHERE uc.id = ? AND uc.user_id = ?",
                CouponDaoImpl::mapUserCoupon, userCouponId, userId);
    }

    /**
     * 供订单模块复用：校验优惠券可用（未使用、有效期内、满足门槛），不满足抛 3003。
     */
    static void checkUsable(UserCouponVO coupon, BigDecimal orderAmount) {
        if (coupon == null) throw new BizException(ResultCode.COUPON_UNUSABLE, "优惠券不存在");
        if (coupon.getStatus() == null || coupon.getStatus() != UserCouponVO.STATUS_UNUSED) {
            throw new BizException(ResultCode.COUPON_UNUSABLE, "优惠券已使用或已过期");
        }
        Timestamp end = DateUtil.parse(coupon.getEndTime());
        if (end != null && end.getTime() < System.currentTimeMillis()) {
            throw new BizException(ResultCode.COUPON_UNUSABLE, "优惠券已过期");
        }
        BigDecimal threshold = coupon.getThreshold() == null ? BigDecimal.ZERO : coupon.getThreshold();
        if (orderAmount == null || orderAmount.compareTo(threshold) < 0) {
            throw new BizException(ResultCode.COUPON_UNUSABLE, "未达到优惠券使用门槛");
        }
    }

    @Override
    public Result<List<CouponVO>> listAvailableCoupons(Long userId) {
        return execute(conn -> {
            requireLogin(userId);
            // 是否已领取用相关子查询标记
            return query(conn,
                    "SELECT c.*, (SELECT COUNT(*) FROM t_user_coupon uc WHERE uc.coupon_id = c.id AND uc.user_id = ?) AS received_count "
                            + "FROM t_coupon c WHERE c.status = 1 AND c.start_time <= NOW() AND c.end_time >= NOW() AND c.remain > 0 "
                            + "ORDER BY c.id DESC",
                    rs -> {
                        CouponVO vo = mapCoupon(rs);
                        vo.setReceived(rs.getInt("received_count") > 0);
                        return vo;
                    }, userId);
        });
    }

    @Override
    public Result<Void> receiveCoupon(Long userId, Long couponId) {
        return executeTx(conn -> {
            requireLogin(userId);
            requireNotNull(couponId, "优惠券 ID");
            CouponVO coupon = queryOne(conn,
                    "SELECT * FROM t_coupon WHERE id = ? AND status = 1 AND start_time <= NOW() AND end_time >= NOW()",
                    CouponDaoImpl::mapCoupon, couponId);
            if (coupon == null || coupon.getRemain() == null || coupon.getRemain() <= 0) {
                throw new BizException(ResultCode.COUPON_INVALID);
            }
            long received = queryLong(conn, "SELECT COUNT(*) FROM t_user_coupon WHERE user_id=? AND coupon_id=?",
                    userId, couponId);
            int perLimit = coupon.getPerLimit() == null ? 1 : coupon.getPerLimit();
            if (received >= perLimit) {
                throw new BizException(ResultCode.COUPON_LIMIT);
            }
            // remain > 0 条件避免并发超发
            int rows = update(conn, "UPDATE t_coupon SET remain = remain - 1 WHERE id=? AND remain > 0", couponId);
            if (rows == 0) throw new BizException(ResultCode.COUPON_INVALID, "优惠券已领完");
            update(conn, "INSERT INTO t_user_coupon(user_id, coupon_id, status, receive_time) VALUES(?, ?, 0, NOW())",
                    userId, couponId);
            return null;
        });
    }

    @Override
    public Result<List<UserCouponVO>> listMyCoupons(Long userId, Integer status) {
        return execute(conn -> {
            requireLogin(userId);
            // 先把已到期但仍标记为未使用的券置为过期
            update(conn, "UPDATE t_user_coupon uc JOIN t_coupon c ON uc.coupon_id = c.id "
                    + "SET uc.status = 2 WHERE uc.user_id = ? AND uc.status = 0 AND c.end_time < NOW()", userId);
            StringBuilder sql = new StringBuilder(SELECT_USER_COUPON + "WHERE uc.user_id = ?");
            List<Object> params = new ArrayList<>();
            params.add(userId);
            if (status != null) {
                sql.append(" AND uc.status = ?");
                params.add(status);
            }
            sql.append(" ORDER BY uc.status ASC, uc.id DESC");
            return query(conn, sql.toString(), CouponDaoImpl::mapUserCoupon, params.toArray());
        });
    }

    @Override
    public Result<List<UserCouponVO>> listUsableCoupons(Long userId, BigDecimal orderAmount) {
        return execute(conn -> {
            requireLogin(userId);
            BigDecimal amount = orderAmount == null ? BigDecimal.ZERO : orderAmount;
            return query(conn,
                    SELECT_USER_COUPON + "WHERE uc.user_id = ? AND uc.status = 0 AND c.start_time <= NOW() AND c.end_time >= NOW() "
                            + "AND c.threshold <= ? ORDER BY c.threshold DESC, uc.id DESC",
                    CouponDaoImpl::mapUserCoupon, userId, amount);
        });
    }

    @Override
    public BigDecimal calcDiscount(UserCouponVO coupon, BigDecimal orderAmount) {
        if (coupon == null || orderAmount == null) return BigDecimal.ZERO;
        BigDecimal threshold = coupon.getThreshold() == null ? BigDecimal.ZERO : coupon.getThreshold();
        if (orderAmount.compareTo(threshold) < 0) return BigDecimal.ZERO;

        BigDecimal discount;
        if (coupon.getType() != null && coupon.getType() == Coupon.TYPE_RATE) {
            // 折扣券：orderAmount × (1 − discountRate)
            BigDecimal rate = coupon.getDiscountRate() == null ? BigDecimal.ONE : coupon.getDiscountRate();
            discount = orderAmount.multiply(BigDecimal.ONE.subtract(rate)).setScale(2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0) discount = BigDecimal.ZERO;
        // 优惠金额不超过订单金额
        if (discount.compareTo(orderAmount) > 0) discount = orderAmount;
        return MoneyUtil.scale(discount);
    }

    /** 新增 / 修改共用校验 */
    private void validate(Coupon c) {
        requireNotNull(c, "优惠券");
        requireNotBlank(c.getName(), "优惠券名称");
        if (c.getType() == null || (c.getType() != Coupon.TYPE_CASH && c.getType() != Coupon.TYPE_RATE)) {
            throw new BizException(ResultCode.BAD_REQUEST, "优惠券类型不正确");
        }
        if (c.getType() == Coupon.TYPE_CASH && !MoneyUtil.isPositive(c.getDiscountAmount())) {
            throw new BizException(ResultCode.BAD_REQUEST, "满减金额必须大于 0");
        }
        if (c.getType() == Coupon.TYPE_RATE) {
            BigDecimal rate = c.getDiscountRate();
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) >= 0) {
                throw new BizException(ResultCode.BAD_REQUEST, "折扣率需在 0~1 之间，如 0.9 表示 9 折");
            }
        }
        if (c.getTotal() == null || c.getTotal() <= 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "发放总量必须大于 0");
        }
        Timestamp start = DateUtil.parse(c.getStartTime());
        Timestamp end = DateUtil.parse(c.getEndTime());
        if (start == null || end == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "时间格式应为 yyyy-MM-dd HH:mm:ss");
        }
        if (!end.after(start)) {
            throw new BizException(ResultCode.BAD_REQUEST, "失效时间必须晚于生效时间");
        }
    }

    @Override
    public Result<CouponVO> addCoupon(Long adminId, Coupon coupon) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            validate(coupon);
            long id = insert(conn,
                    "INSERT INTO t_coupon(name, type, threshold, discount_amount, discount_rate, total, remain, per_limit, start_time, end_time, status, create_time) "
                            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                    coupon.getName().trim(), coupon.getType(),
                    MoneyUtil.scale(coupon.getThreshold()),
                    MoneyUtil.scale(coupon.getDiscountAmount()),
                    coupon.getDiscountRate() == null ? BigDecimal.ONE : coupon.getDiscountRate(),
                    coupon.getTotal(), coupon.getTotal(),
                    coupon.getPerLimit() == null || coupon.getPerLimit() <= 0 ? 1 : coupon.getPerLimit(),
                    DateUtil.parse(coupon.getStartTime()), DateUtil.parse(coupon.getEndTime()),
                    coupon.getStatus() == null ? 1 : coupon.getStatus());
            return findById(conn, id);
        });
    }

    @Override
    public Result<CouponVO> updateCoupon(Long adminId, Coupon coupon) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            requireNotNull(coupon, "优惠券");
            requireNotNull(coupon.getId(), "优惠券 ID");
            validate(coupon);
            CouponVO old = findById(conn, coupon.getId());
            if (old == null) throw new BizException(ResultCode.NOT_FOUND, "优惠券不存在");
            // 总量调整时同步调整剩余数量（保持已领取数不变）
            int issued = old.getTotal() - old.getRemain();
            int remain = Math.max(coupon.getTotal() - issued, 0);
            update(conn,
                    "UPDATE t_coupon SET name=?, type=?, threshold=?, discount_amount=?, discount_rate=?, total=?, remain=?, per_limit=?, start_time=?, end_time=?, status=? WHERE id=?",
                    coupon.getName().trim(), coupon.getType(),
                    MoneyUtil.scale(coupon.getThreshold()),
                    MoneyUtil.scale(coupon.getDiscountAmount()),
                    coupon.getDiscountRate() == null ? BigDecimal.ONE : coupon.getDiscountRate(),
                    coupon.getTotal(), remain,
                    coupon.getPerLimit() == null || coupon.getPerLimit() <= 0 ? 1 : coupon.getPerLimit(),
                    DateUtil.parse(coupon.getStartTime()), DateUtil.parse(coupon.getEndTime()),
                    coupon.getStatus() == null ? old.getStatus() : coupon.getStatus(),
                    coupon.getId());
            return findById(conn, coupon.getId());
        });
    }

    @Override
    public Result<Void> deleteCoupon(Long adminId, Long id) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            requireNotNull(id, "优惠券 ID");
            long received = queryLong(conn, "SELECT COUNT(*) FROM t_user_coupon WHERE coupon_id = ?", id);
            if (received > 0) {
                throw new BizException(ResultCode.BAD_REQUEST, "该优惠券已被领取，只能停用不能删除");
            }
            int rows = update(conn, "DELETE FROM t_coupon WHERE id = ?", id);
            if (rows == 0) throw new BizException(ResultCode.NOT_FOUND, "优惠券不存在");
            return null;
        });
    }

    @Override
    public Result<PageResult<CouponVO>> listAllCoupons(Long adminId, int page, int size) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            return queryPage(conn, "SELECT COUNT(*) FROM t_coupon",
                    "SELECT * FROM t_coupon ORDER BY id DESC", CouponDaoImpl::mapCoupon, page, size);
        });
    }
}
