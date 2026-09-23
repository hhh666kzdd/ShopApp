package com.example.shopapp.dao;

import com.example.shopapp.common.Result;
import com.example.shopapp.vo.CartItemVO;
import com.example.shopapp.vo.CartSummary;

import java.util.List;

/**
 * 购物车模块（接口文档 5）。
 */
public interface CartDao {

    /** 5.1 购物车列表：联查商品表得到最新价格、库存、上架状态 */
    Result<List<CartItemVO>> listCart(Long userId);

    /** 5.2 加入购物车：同一商品已存在则数量累加；数量不能超过库存。失败码 2001、2002 */
    Result<Void> addToCart(Long userId, Long productId, Integer quantity);

    /** 5.3 修改数量：quantity >= 1 且 <= 库存。失败码 400、2002 */
    Result<Void> updateQuantity(Long userId, Long cartId, Integer quantity);

    /** 5.4 勾选 / 取消勾选 */
    Result<Void> updateChecked(Long userId, Long cartId, Integer checked);

    /** 5.5 全选 / 全不选 */
    Result<Void> checkAll(Long userId, Integer checked);

    /** 5.6 删除购物车记录 */
    Result<Void> deleteCartItem(Long userId, Long cartId);

    /** 5.7 批量删除（结算后清除），下单成功后由订单模块调用 */
    Result<Void> deleteByProductIds(Long userId, List<Long> productIds);

    /** 5.8 已勾选合计 */
    Result<CartSummary> getCheckedSummary(Long userId);
}
