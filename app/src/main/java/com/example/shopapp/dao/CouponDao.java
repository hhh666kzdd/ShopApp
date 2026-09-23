package com.example.shopapp.dao;

import com.example.shopapp.common.PageResult;
import com.example.shopapp.common.Result;
import com.example.shopapp.entity.Coupon;
import com.example.shopapp.vo.CouponVO;
import com.example.shopapp.vo.UserCouponVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券模块（接口文档 6.1）。
 */
public interface CouponDao {

    /** 6.1.1 领券中心：status=1、在有效期内、remain>0 的优惠券，并标记当前用户是否已领取 */
    Result<List<CouponVO>> listAvailableCoupons(Long userId);

    /** 6.1.2 领取优惠券：校验有效期、剩余数量、每人限领。失败码 3001、3002 */
    Result<Void> receiveCoupon(Long userId, Long couponId);

    /**
     * 6.1.3 我的优惠券。
     *
     * @param status 0 未使用，1 已使用，2 已过期；null 表示全部
     */
    Result<List<UserCouponVO>> listMyCoupons(Long userId, Integer status);

    /** 6.1.4 订单可用优惠券：status=0、在有效期内、threshold <= orderAmount */
    Result<List<UserCouponVO>> listUsableCoupons(Long userId, BigDecimal orderAmount);

    /**
     * 6.1.5 计算优惠金额（纯计算，不访问数据库）。
     * 满减券：orderAmount >= threshold 时优惠 discountAmount，否则 0；
     * 折扣券：orderAmount >= threshold 时优惠 orderAmount × (1 − discountRate)，四舍五入保留 2 位；
     * 优惠金额不超过订单金额。
     */
    BigDecimal calcDiscount(UserCouponVO coupon, BigDecimal orderAmount);

    /** 6.1.6 新增优惠券【管理员】，remain 初始等于 total */
    Result<CouponVO> addCoupon(Long adminId, Coupon coupon);

    /** 6.1.6 修改优惠券【管理员】 */
    Result<CouponVO> updateCoupon(Long adminId, Coupon coupon);

    /** 6.1.6 删除优惠券【管理员】：已被领取的仅允许停用 */
    Result<Void> deleteCoupon(Long adminId, Long id);

    /** 6.1.6 后台优惠券列表【管理员】：含停用、过期 */
    Result<PageResult<CouponVO>> listAllCoupons(Long adminId, int page, int size);
}
