package com.example.shopapp.dao;

import com.example.shopapp.common.PageResult;
import com.example.shopapp.common.Result;
import com.example.shopapp.req.CreateOrderReq;
import com.example.shopapp.req.OrderItemReq;
import com.example.shopapp.vo.OrderPreviewVO;
import com.example.shopapp.vo.OrderVO;

import java.util.List;

/**
 * 订单模块（接口文档 8）。
 */
public interface OrderDao {

    /**
     * 8.2 订单预览（结算页）。
     *
     * @param addressId    为 null 时取默认地址
     * @param userCouponId 可为 null
     */
    Result<OrderPreviewVO> previewOrder(Long userId, List<OrderItemReq> items, Long addressId, Long userCouponId);

    /** 8.3 创建订单：在一个 JDBC 事务中执行，任一步失败回滚 */
    Result<OrderVO> createOrder(Long userId, CreateOrderReq req);

    /** 8.4 我的订单列表：按创建时间倒序，每个订单携带明细。status 为 null 表示全部 */
    Result<PageResult<OrderVO>> listOrders(Long userId, Integer status, int page, int size);

    /** 8.5 订单详情。失败码 4001 */
    Result<OrderVO> getOrderDetail(Long userId, Long orderId);

    /** 8.6 支付订单（模拟支付）：仅 status=0 可支付；支付后 status=1。失败码 4002 */
    Result<Void> payOrder(Long userId, Long orderId);

    /** 8.7 取消订单：仅 status=0 可取消；恢复库存与销量，退还优惠券。失败码 4002 */
    Result<Void> cancelOrder(Long userId, Long orderId);

    /** 8.8 确认收货：仅 status=2 可确认；确认后 status=3。失败码 4002 */
    Result<Void> confirmReceipt(Long userId, Long orderId);

    /** 8.9 后台订单列表【管理员】：可按状态、订单号筛选，附带下单用户名 */
    Result<PageResult<OrderVO>> listAllOrders(Long adminId, Integer status, String orderNo, int page, int size);

    /** 8.10 订单发货【管理员】：仅 status=1 可发货；发货后 status=2。失败码 4002 */
    Result<Void> shipOrder(Long adminId, Long orderId);
}
