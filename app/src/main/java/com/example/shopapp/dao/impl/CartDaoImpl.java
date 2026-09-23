package com.example.shopapp.dao.impl;

import com.example.shopapp.common.BizException;
import com.example.shopapp.common.Result;
import com.example.shopapp.common.ResultCode;
import com.example.shopapp.dao.CartDao;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.CartItemVO;
import com.example.shopapp.vo.CartSummary;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 购物车模块实现（接口文档 5）。
 */
public class CartDaoImpl extends BaseDao implements CartDao {

    /** 购物车联查商品 + 活动价 */
    private static final String SELECT_CART =
            "SELECT c.id, c.product_id, c.quantity, c.checked, p.name, p.main_image, p.price, p.stock, p.status, "
                    + ProductDaoImpl.PROMO_PRICE_SUB + " AS promo_price "
                    + "FROM t_cart c JOIN t_product p ON c.product_id = p.id ";

    private static CartItemVO mapCart(ResultSet rs) throws SQLException {
        CartItemVO vo = new CartItemVO();
        vo.setId(rs.getLong("id"));
        vo.setProductId(rs.getLong("product_id"));
        vo.setProductName(rs.getString("name"));
        vo.setProductImage(rs.getString("main_image"));
        vo.setPrice(rs.getBigDecimal("price"));
        BigDecimal promo = rs.getBigDecimal("promo_price");
        vo.setCurrentPrice(promo != null ? promo : vo.getPrice());
        vo.setQuantity(getInt(rs, "quantity"));
        vo.setChecked(getInt(rs, "checked"));
        vo.setStock(getInt(rs, "stock"));
        vo.setStatus(getInt(rs, "status"));
        int qty = vo.getQuantity() == null ? 0 : vo.getQuantity();
        vo.setSubtotal(MoneyUtil.scale(vo.getCurrentPrice().multiply(BigDecimal.valueOf(qty))));
        return vo;
    }

    /** 查询商品库存与状态，用于数量校验 */
    private int[] productStockAndStatus(Connection conn, Long productId) throws SQLException {
        int[] r = queryOne(conn, "SELECT stock, status FROM t_product WHERE id = ?",
                rs -> new int[]{rs.getInt("stock"), rs.getInt("status")}, productId);
        if (r == null || r[1] != 1) throw new BizException(ResultCode.PRODUCT_OFF);
        return r;
    }

    @Override
    public Result<List<CartItemVO>> listCart(Long userId) {
        return execute(conn -> {
            requireLogin(userId);
            return query(conn, SELECT_CART + "WHERE c.user_id = ? ORDER BY c.create_time DESC, c.id DESC",
                    CartDaoImpl::mapCart, userId);
        });
    }

    @Override
    public Result<Void> addToCart(Long userId, Long productId, Integer quantity) {
        return execute(conn -> {
            requireLogin(userId);
            requireNotNull(productId, "商品 ID");
            int qty = quantity == null ? 1 : quantity;
            if (qty < 1) throw new BizException(ResultCode.BAD_REQUEST, "数量至少为 1");
            int stock = productStockAndStatus(conn, productId)[0];

            Object[] existing = queryOne(conn, "SELECT id, quantity FROM t_cart WHERE user_id=? AND product_id=?",
                    rs -> new Object[]{rs.getLong("id"), rs.getInt("quantity")}, userId, productId);
            if (existing != null) {
                // 已存在：数量累加，累加后不能超过库存
                int newQty = (Integer) existing[1] + qty;
                if (newQty > stock) throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
                update(conn, "UPDATE t_cart SET quantity = ?, checked = 1, update_time = NOW() WHERE id = ?",
                        newQty, existing[0]);
            } else {
                if (qty > stock) throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
                update(conn, "INSERT INTO t_cart(user_id, product_id, quantity, checked, create_time) VALUES(?, ?, ?, 1, NOW())",
                        userId, productId, qty);
            }
            return null;
        });
    }

    @Override
    public Result<Void> updateQuantity(Long userId, Long cartId, Integer quantity) {
        return execute(conn -> {
            requireLogin(userId);
            requireNotNull(cartId, "购物车 ID");
            if (quantity == null || quantity < 1) {
                throw new BizException(ResultCode.BAD_REQUEST, "数量至少为 1");
            }
            Long productId = queryOne(conn, "SELECT product_id FROM t_cart WHERE id = ? AND user_id = ?",
                    rs -> rs.getLong("product_id"), cartId, userId);
            if (productId == null) throw new BizException(ResultCode.NOT_FOUND, "购物车记录不存在");
            int stock = productStockAndStatus(conn, productId)[0];
            if (quantity > stock) throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
            update(conn, "UPDATE t_cart SET quantity = ?, update_time = NOW() WHERE id = ? AND user_id = ?",
                    quantity, cartId, userId);
            return null;
        });
    }

    @Override
    public Result<Void> updateChecked(Long userId, Long cartId, Integer checked) {
        return execute(conn -> {
            requireLogin(userId);
            requireNotNull(cartId, "购物车 ID");
            int c = (checked != null && checked == 1) ? 1 : 0;
            int rows = update(conn, "UPDATE t_cart SET checked = ?, update_time = NOW() WHERE id = ? AND user_id = ?",
                    c, cartId, userId);
            if (rows == 0) throw new BizException(ResultCode.NOT_FOUND, "购物车记录不存在");
            return null;
        });
    }

    @Override
    public Result<Void> checkAll(Long userId, Integer checked) {
        return execute(conn -> {
            requireLogin(userId);
            int c = (checked != null && checked == 1) ? 1 : 0;
            update(conn, "UPDATE t_cart SET checked = ?, update_time = NOW() WHERE user_id = ?", c, userId);
            return null;
        });
    }

    @Override
    public Result<Void> deleteCartItem(Long userId, Long cartId) {
        return execute(conn -> {
            requireLogin(userId);
            requireNotNull(cartId, "购物车 ID");
            int rows = update(conn, "DELETE FROM t_cart WHERE id = ? AND user_id = ?", cartId, userId);
            if (rows == 0) throw new BizException(ResultCode.NOT_FOUND, "购物车记录不存在");
            return null;
        });
    }

    @Override
    public Result<Void> deleteByProductIds(Long userId, List<Long> productIds) {
        return execute(conn -> {
            requireLogin(userId);
            deleteByProductIds(conn, userId, productIds);
            return null;
        });
    }

    /** 供订单模块在同一事务中调用 */
    static void deleteByProductIds(Connection conn, Long userId, List<Long> productIds) throws SQLException {
        if (productIds == null || productIds.isEmpty()) return;
        StringBuilder sql = new StringBuilder("DELETE FROM t_cart WHERE user_id = ? AND product_id IN (");
        Object[] params = new Object[productIds.size() + 1];
        params[0] = userId;
        for (int i = 0; i < productIds.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
            params[i + 1] = productIds.get(i);
        }
        sql.append(")");
        new CartDaoImpl().update(conn, sql.toString(), params);
    }

    @Override
    public Result<CartSummary> getCheckedSummary(Long userId) {
        return execute(conn -> {
            requireLogin(userId);
            // 只统计已勾选且上架的商品
            List<CartItemVO> items = query(conn,
                    SELECT_CART + "WHERE c.user_id = ? AND c.checked = 1 AND p.status = 1",
                    CartDaoImpl::mapCart, userId);
            int count = 0;
            BigDecimal amount = BigDecimal.ZERO;
            for (CartItemVO item : items) {
                count += item.getQuantity();
                amount = amount.add(item.getSubtotal());
            }
            return new CartSummary(count, MoneyUtil.scale(amount));
        });
    }
}
