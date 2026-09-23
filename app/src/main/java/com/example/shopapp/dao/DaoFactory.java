package com.example.shopapp.dao;

import com.example.shopapp.dao.impl.AddressDaoImpl;
import com.example.shopapp.dao.impl.AdminDaoImpl;
import com.example.shopapp.dao.impl.CartDaoImpl;
import com.example.shopapp.dao.impl.CategoryDaoImpl;
import com.example.shopapp.dao.impl.CouponDaoImpl;
import com.example.shopapp.dao.impl.OrderDaoImpl;
import com.example.shopapp.dao.impl.ProductDaoImpl;
import com.example.shopapp.dao.impl.PromotionDaoImpl;
import com.example.shopapp.dao.impl.UserDaoImpl;

/**
 * DAO 工厂：UI 层通过此处获取各模块 DAO 单例，避免直接依赖实现类。
 */
public final class DaoFactory {

    private static final UserDao USER_DAO = new UserDaoImpl();
    private static final CategoryDao CATEGORY_DAO = new CategoryDaoImpl();
    private static final ProductDao PRODUCT_DAO = new ProductDaoImpl();
    private static final CartDao CART_DAO = new CartDaoImpl();
    private static final AddressDao ADDRESS_DAO = new AddressDaoImpl();
    private static final CouponDao COUPON_DAO = new CouponDaoImpl();
    private static final PromotionDao PROMOTION_DAO = new PromotionDaoImpl();
    private static final OrderDao ORDER_DAO = new OrderDaoImpl();
    private static final AdminDao ADMIN_DAO = new AdminDaoImpl();

    private DaoFactory() {
    }

    public static UserDao user() {
        return USER_DAO;
    }

    public static CategoryDao category() {
        return CATEGORY_DAO;
    }

    public static ProductDao product() {
        return PRODUCT_DAO;
    }

    public static CartDao cart() {
        return CART_DAO;
    }

    public static AddressDao address() {
        return ADDRESS_DAO;
    }

    public static CouponDao coupon() {
        return COUPON_DAO;
    }

    public static PromotionDao promotion() {
        return PROMOTION_DAO;
    }

    public static OrderDao order() {
        return ORDER_DAO;
    }

    public static AdminDao admin() {
        return ADMIN_DAO;
    }
}
