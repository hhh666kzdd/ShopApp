package com.example.shopapp.dao.impl;

import com.example.shopapp.common.BizException;
import com.example.shopapp.common.PageResult;
import com.example.shopapp.common.Result;
import com.example.shopapp.common.ResultCode;
import com.example.shopapp.dao.OrderDao;
import com.example.shopapp.entity.Address;
import com.example.shopapp.req.CreateOrderReq;
import com.example.shopapp.req.OrderItemReq;
import com.example.shopapp.util.DateUtil;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.OrderItemVO;
import com.example.shopapp.vo.OrderPreviewVO;
import com.example.shopapp.vo.OrderVO;
import com.example.shopapp.vo.UserCouponVO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单模块实现（接口文档 8）。
 */
public class OrderDaoImpl extends BaseDao implements OrderDao {

    private final CouponDaoImpl couponDao = new CouponDaoImpl();

    /** 订单联查下单用户名 */
    private static final String SELECT_ORDER =
            "SELECT o.*, u.username FROM t_order o LEFT JOIN t_user u ON o.user_id = u.id ";

    static OrderVO mapOrder(ResultSet rs) throws SQLException {
        OrderVO vo = new OrderVO();
        vo.setId(rs.getLong("id"));
        vo.setOrderNo(rs.getString("order_no"));
        vo.setUserId(rs.getLong("user_id"));
        vo.setUsername(rs.getString("username"));
        vo.setStatus(getInt(rs, "status"));
        vo.setTotalAmount(rs.getBigDecimal("total_amount"));
        vo.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        vo.setPayAmount(rs.getBigDecimal("pay_amount"));
        vo.setUserCouponId(getLong(rs, "user_coupon_id"));
        vo.setReceiverName(rs.getString("receiver_name"));
        vo.setReceiverPhone(rs.getString("receiver_phone"));
        vo.setReceiverAddress(rs.getString("receiver_address"));
        vo.setRemark(rs.getString("remark"));
        vo.setPayTime(getTime(rs, "pay_time"));
        vo.setShipTime(getTime(rs, "ship_time"));
        vo.setFinishTime(getTime(rs, "finish_time"));
        vo.setCreateTime(getTime(rs, "create_time"));
        return vo;
    }

    static OrderItemVO mapOrderItem(ResultSet rs) throws SQLException {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(rs.getLong("id"));
        vo.setProductId(rs.getLong("product_id"));
        vo.setProductName(rs.getString("product_name"));
        vo.setProductImage(rs.getString("product_image"));
        vo.setPrice(rs.getBigDecimal("price"));
        vo.setQuantity(getInt(rs, "quantity"));
        vo.setTotalPrice(rs.getBigDecimal("total_price"));
        return vo;
    }

    /** 为一批订单一次性加载明细 */
    private void fillItems(Connection conn, List<OrderVO> orders) throws SQLException {
        if (orders.isEmpty()) return;
        Map<Long, OrderVO> map = new HashMap<>();
        StringBuilder in = new StringBuilder();
        Object[] params = new Object[orders.size()];
        for (int i = 0; i < orders.size(); i++) {
            map.put(orders.get(i).getId(), orders.get(i));
            in.append(i == 0 ? "?" : ", ?");
            params[i] = orders.get(i).getId();
        }
        List<Object[]> rows = query(conn,
                "SELECT * FROM t_order_item WHERE order_id IN (" + in + ") ORDER BY id ASC",
                rs -> new Object[]{rs.getLong("order_id"), mapOrderItem(rs)}, params);
        for (Object[] row : rows) {
            OrderVO order = map.get((Long) row[0]);
            if (order != null) order.getItems().add((OrderItemVO) row[1]);
        }
    }

    /** 查询订单（可限定用户），并加载明细；不存在返回 null */
    private OrderVO findOrder(Connection conn, Long orderId, Long userId) throws SQLException {
        String sql = SELECT_ORDER + "WHERE o.id = ?" + (userId == null ? "" : " AND o.user_id = ?");
        OrderVO order = userId == null
                ? queryOne(conn, sql, OrderDaoImpl::mapOrder, orderId)
                : queryOne(conn, sql, OrderDaoImpl::mapOrder, orderId, userId);
        if (order != null) {
            List<OrderVO> one = new ArrayList<>();
            one.add(order);
            fillItems(conn, one);
        }
        return order;
    }

    /** 商品快照，用于校验与写入明细 */
    private static class ProductSnapshot {
        long id;
        String name;
        String image;
        int stock;
        int status;
        BigDecimal price; // 现价
    }

    /**
     * 校验商品并按现价计算明细（预览与下单共用）。
     *
     * @param lock 下单时为 true，使用 SELECT ... FOR UPDATE 锁定商品行
     */
    private List<OrderItemVO> buildItems(Connection conn, List<OrderItemReq> reqs, boolean lock) throws SQLException {
        if (reqs == null || reqs.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "请选择要购买的商品");
        }
        List<OrderItemVO> items = new ArrayList<>();
        for (OrderItemReq req : reqs) {
            requireNotNull(req.getProductId(), "商品 ID");
            int qty = req.getQuantity() == null ? 0 : req.getQuantity();
            if (qty < 1) throw new BizException(ResultCode.BAD_REQUEST, "购买数量至少为 1");

            ProductSnapshot p = queryOne(conn,
                    "SELECT id, name, main_image, stock, status, price FROM t_product WHERE id = ?" + (lock ? " FOR UPDATE" : ""),
                    rs -> {
                        ProductSnapshot s = new ProductSnapshot();
                        s.id = rs.getLong("id");
                        s.name = rs.getString("name");
                        s.image = rs.getString("main_image");
                        s.stock = rs.getInt("stock");
                        s.status = rs.getInt("status");
                        s.price = rs.getBigDecimal("price");
                        return s;
                    }, req.getProductId());
            if (p == null || p.status != 1) {
                throw new BizException(ResultCode.PRODUCT_OFF);
            }
            if (p.stock < qty) {
                throw new BizException(ResultCode.STOCK_NOT_ENOUGH, "「" + p.name + "」库存不足");
            }
            // 统一按现价（含活动价）计价
            BigDecimal price = PromotionDaoImpl.effectivePrice(conn, p.id);
            if (price == null) price = p.price;

            OrderItemVO item = new OrderItemVO();
            item.setProductId(p.id);
            item.setProductName(p.name);
            item.setProductImage(p.image);
            item.setPrice(MoneyUtil.scale(price));
            item.setQuantity(qty);
            item.setTotalPrice(MoneyUtil.scale(price.multiply(BigDecimal.valueOf(qty))));
            items.add(item);
        }
        return items;
    }

    private static BigDecimal sum(List<OrderItemVO> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemVO item : items) total = total.add(item.getTotalPrice());
        return MoneyUtil.scale(total);
    }

    @Override
    public Result<OrderPreviewVO> previewOrder(Long userId, List<OrderItemReq> items, Long addressId, Long userCouponId) {
        return execute(conn -> {
            requireLogin(userId);
            OrderPreviewVO vo = new OrderPreviewVO();
            vo.setItems(buildItems(conn, items, false));
            BigDecimal total = sum(vo.getItems());

            // 地址：指定则校验归属，否则取默认地址（可能为 null）
            if (addressId != null) {
                Address address = AddressDaoImpl.findByIdAndUser(conn, userId, addressId);
                if (address == null) throw new BizException(ResultCode.ADDRESS_NOT_FOUND);
                vo.setAddress(address);
            } else {
                vo.setAddress(AddressDaoImpl.findDefault(conn, userId));
            }

            BigDecimal discount = BigDecimal.ZERO;
            if (userCouponId != null) {
                UserCouponVO coupon = CouponDaoImpl.findUserCoupon(conn, userId, userCouponId);
                CouponDaoImpl.checkUsable(coupon, total);
                discount = couponDao.calcDiscount(coupon, total);
                vo.setCoupon(coupon);
            }
            vo.setTotalAmount(total);
            vo.setDiscountAmount(discount);
            vo.setPayAmount(MoneyUtil.scale(total.subtract(discount)));
            return vo;
        });
    }

    @Override
    public Result<OrderVO> createOrder(Long userId, CreateOrderReq req) {
        return executeTx(conn -> {
            requireLogin(userId);
            requireNotNull(req, "下单参数");
            requireNotNull(req.getAddressId(), "收货地址");

            // 1. 校验地址属于当前用户
            Address address = AddressDaoImpl.findByIdAndUser(conn, userId, req.getAddressId());
            if (address == null) throw new BizException(ResultCode.ADDRESS_NOT_FOUND);

            // 2. 校验商品上架状态与库存，按现价计价（锁定商品行）
            List<OrderItemVO> items = buildItems(conn, req.getItems(), true);
            BigDecimal total = sum(items);

            // 3. 扣减库存、增加销量，影响行数为 0 视为库存不足
            for (OrderItemVO item : items) {
                int rows = update(conn,
                        "UPDATE t_product SET stock = stock - ?, sales = sales + ?, update_time = NOW() WHERE id = ? AND stock >= ?",
                        item.getQuantity(), item.getQuantity(), item.getProductId(), item.getQuantity());
                if (rows == 0) {
                    throw new BizException(ResultCode.STOCK_NOT_ENOUGH, "「" + item.getProductName() + "」库存不足");
                }
            }

            // 4. 优惠券校验与计算
            BigDecimal discount = BigDecimal.ZERO;
            if (req.getUserCouponId() != null) {
                UserCouponVO coupon = queryOne(conn,
                        CouponDaoImpl.SELECT_USER_COUPON + "WHERE uc.id = ? AND uc.user_id = ? FOR UPDATE",
                        CouponDaoImpl::mapUserCoupon, req.getUserCouponId(), userId);
                CouponDaoImpl.checkUsable(coupon, total);
                discount = couponDao.calcDiscount(coupon, total);
            }
            BigDecimal payAmount = MoneyUtil.scale(total.subtract(discount));

            // 5. 写入订单与明细（商品名称、图片、单价快照）
            String orderNo = DateUtil.genOrderNo();
            long orderId = insert(conn,
                    "INSERT INTO t_order(order_no, user_id, status, total_amount, discount_amount, pay_amount, user_coupon_id, "
                            + "receiver_name, receiver_phone, receiver_address, remark, create_time) "
                            + "VALUES(?, ?, 0, ?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                    orderNo, userId, total, MoneyUtil.scale(discount), payAmount, req.getUserCouponId(),
                    address.getReceiverName(), address.getReceiverPhone(), address.getFullAddress(), req.getRemark());
            List<Long> productIds = new ArrayList<>();
            for (OrderItemVO item : items) {
                update(conn,
                        "INSERT INTO t_order_item(order_id, product_id, product_name, product_image, price, quantity, total_price) "
                                + "VALUES(?, ?, ?, ?, ?, ?, ?)",
                        orderId, item.getProductId(), item.getProductName(), item.getProductImage(),
                        item.getPrice(), item.getQuantity(), item.getTotalPrice());
                productIds.add(item.getProductId());
            }

            // 优惠券标记为已使用并关联订单
            if (req.getUserCouponId() != null) {
                update(conn, "UPDATE t_user_coupon SET status = 1, order_id = ?, use_time = NOW() WHERE id = ? AND status = 0",
                        orderId, req.getUserCouponId());
            }

            // 6. 来自购物车结算时删除对应购物车记录
            if (req.isFromCart()) {
                CartDaoImpl.deleteByProductIds(conn, userId, productIds);
            }

            // 7. 返回订单
            return findOrder(conn, orderId, userId);
        });
    }

    @Override
    public Result<PageResult<OrderVO>> listOrders(Long userId, Integer status, int page, int size) {
        return execute(conn -> {
            requireLogin(userId);
            StringBuilder where = new StringBuilder(" WHERE o.user_id = ?");
            List<Object> params = new ArrayList<>();
            params.add(userId);
            if (status != null) {
                where.append(" AND o.status = ?");
                params.add(status);
            }
            PageResult<OrderVO> result = queryPage(conn,
                    "SELECT COUNT(*) FROM t_order o" + where,
                    SELECT_ORDER + where + " ORDER BY o.create_time DESC, o.id DESC",
                    OrderDaoImpl::mapOrder, page, size, params.toArray());
            fillItems(conn, result.getRecords());
            return result;
        });
    }

    @Override
    public Result<OrderVO> getOrderDetail(Long userId, Long orderId) {
        return execute(conn -> {
            requireLogin(userId);
            requireNotNull(orderId, "订单 ID");
            OrderVO order = findOrder(conn, orderId, userId);
            if (order == null) throw new BizException(ResultCode.ORDER_NOT_FOUND);
            return order;
        });
    }

    /**
     * 状态流转的公共实现：仅 fromStatus 状态可操作，成功后置为 toStatus 并写入时间字段。
     */
    private void changeStatus(Connection conn, Long orderId, Long userId, int fromStatus, int toStatus,
                              String timeColumn) throws SQLException {
        requireNotNull(orderId, "订单 ID");
        Integer current = userId == null
                ? queryOne(conn, "SELECT status FROM t_order WHERE id = ? FOR UPDATE", rs -> rs.getInt("status"), orderId)
                : queryOne(conn, "SELECT status FROM t_order WHERE id = ? AND user_id = ? FOR UPDATE",
                rs -> rs.getInt("status"), orderId, userId);
        if (current == null) throw new BizException(ResultCode.ORDER_NOT_FOUND);
        if (current != fromStatus) throw new BizException(ResultCode.ORDER_STATUS_ERROR);
        update(conn, "UPDATE t_order SET status = ?, " + timeColumn + " = NOW(), update_time = NOW() WHERE id = ?",
                toStatus, orderId);
    }

    @Override
    public Result<Void> payOrder(Long userId, Long orderId) {
        return executeTx(conn -> {
            requireLogin(userId);
            changeStatus(conn, orderId, userId, OrderVO.STATUS_UNPAID, OrderVO.STATUS_PAID, "pay_time");
            return null;
        });
    }

    @Override
    public Result<Void> cancelOrder(Long userId, Long orderId) {
        return executeTx(conn -> {
            requireLogin(userId);
            OrderVO order = findOrder(conn, orderId, userId);
            if (order == null) throw new BizException(ResultCode.ORDER_NOT_FOUND);
            if (order.getStatus() != OrderVO.STATUS_UNPAID) throw new BizException(ResultCode.ORDER_STATUS_ERROR);

            update(conn, "UPDATE t_order SET status = ?, update_time = NOW() WHERE id = ? AND status = ?",
                    OrderVO.STATUS_CANCELED, orderId, OrderVO.STATUS_UNPAID);
            // 恢复库存与销量
            for (OrderItemVO item : order.getItems()) {
                update(conn, "UPDATE t_product SET stock = stock + ?, sales = GREATEST(sales - ?, 0), update_time = NOW() WHERE id = ?",
                        item.getQuantity(), item.getQuantity(), item.getProductId());
            }
            // 退还优惠券
            if (order.getUserCouponId() != null) {
                update(conn, "UPDATE t_user_coupon SET status = 0, order_id = NULL, use_time = NULL WHERE id = ?",
                        order.getUserCouponId());
            }
            return null;
        });
    }

    @Override
    public Result<Void> confirmReceipt(Long userId, Long orderId) {
        return executeTx(conn -> {
            requireLogin(userId);
            changeStatus(conn, orderId, userId, OrderVO.STATUS_SHIPPED, OrderVO.STATUS_FINISHED, "finish_time");
            return null;
        });
    }

    @Override
    public Result<PageResult<OrderVO>> listAllOrders(Long adminId, Integer status, String orderNo, int page, int size) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            StringBuilder where = new StringBuilder(" WHERE 1 = 1");
            List<Object> params = new ArrayList<>();
            if (status != null) {
                where.append(" AND o.status = ?");
                params.add(status);
            }
            String no = like(orderNo);
            if (no != null) {
                where.append(" AND o.order_no LIKE ?");
                params.add(no);
            }
            PageResult<OrderVO> result = queryPage(conn,
                    "SELECT COUNT(*) FROM t_order o" + where,
                    SELECT_ORDER + where + " ORDER BY o.create_time DESC, o.id DESC",
                    OrderDaoImpl::mapOrder, page, size, params.toArray());
            fillItems(conn, result.getRecords());
            return result;
        });
    }

    @Override
    public Result<Void> shipOrder(Long adminId, Long orderId) {
        return executeTx(conn -> {
            requireAdmin(conn, adminId);
            changeStatus(conn, orderId, null, OrderVO.STATUS_PAID, OrderVO.STATUS_SHIPPED, "ship_time");
            return null;
        });
    }
}
