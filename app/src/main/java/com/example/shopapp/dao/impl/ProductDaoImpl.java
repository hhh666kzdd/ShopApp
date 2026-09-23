package com.example.shopapp.dao.impl;

import com.example.shopapp.common.BizException;
import com.example.shopapp.common.PageResult;
import com.example.shopapp.common.Result;
import com.example.shopapp.common.ResultCode;
import com.example.shopapp.dao.ProductDao;
import com.example.shopapp.entity.Product;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.ProductVO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品模块实现（接口文档 4）。
 * <p>
 * 列表 / 详情通过子查询直接填充进行中的活动价 promo_price 与活动名称 promotion_title，
 * 避免逐条查询活动表。
 */
public class ProductDaoImpl extends BaseDao implements ProductDao {

    /** 进行中活动的活动价（多个活动取最低价），无活动为 NULL */
    static final String PROMO_PRICE_SUB =
            "(SELECT pm.promo_price FROM t_promotion pm WHERE pm.product_id = p.id AND pm.status = 1 "
                    + "AND pm.start_time <= NOW() AND pm.end_time >= NOW() ORDER BY pm.promo_price ASC LIMIT 1)";

    static final String PROMO_TITLE_SUB =
            "(SELECT pm.title FROM t_promotion pm WHERE pm.product_id = p.id AND pm.status = 1 "
                    + "AND pm.start_time <= NOW() AND pm.end_time >= NOW() ORDER BY pm.promo_price ASC LIMIT 1)";

    /** 商品查询公共 SELECT 片段 */
    static final String SELECT_PRODUCT =
            "SELECT p.*, c.name AS category_name, " + PROMO_PRICE_SUB + " AS promo_price, "
                    + PROMO_TITLE_SUB + " AS promotion_title "
                    + "FROM t_product p LEFT JOIN t_category c ON p.category_id = c.id ";

    static ProductVO mapProduct(ResultSet rs) throws SQLException {
        ProductVO vo = new ProductVO();
        vo.setId(rs.getLong("id"));
        vo.setCategoryId(getLong(rs, "category_id"));
        vo.setCategoryName(rs.getString("category_name"));
        vo.setName(rs.getString("name"));
        vo.setSubtitle(rs.getString("subtitle"));
        vo.setMainImage(rs.getString("main_image"));
        vo.setDetail(rs.getString("detail"));
        vo.setPrice(rs.getBigDecimal("price"));
        vo.setPromoPrice(rs.getBigDecimal("promo_price"));
        vo.setPromotionTitle(rs.getString("promotion_title"));
        vo.setStock(getInt(rs, "stock"));
        vo.setSales(getInt(rs, "sales"));
        vo.setStatus(getInt(rs, "status"));
        vo.setCreateTime(getTime(rs, "create_time"));
        return vo;
    }

    /** 供其它 DAO 复用：按 ID 查询商品（含活动价），不存在返回 null */
    static ProductVO findById(Connection conn, Long id) throws SQLException {
        ProductDaoImpl dao = new ProductDaoImpl();
        return dao.queryOne(conn, SELECT_PRODUCT + "WHERE p.id = ?", ProductDaoImpl::mapProduct, id);
    }

    /** 根据 sort 参数生成 ORDER BY 子句，现价 = IFNULL(活动价, 原价) */
    private static String orderBy(String sort) {
        if (SORT_PRICE_ASC.equals(sort)) return " ORDER BY IFNULL(promo_price, p.price) ASC, p.id DESC";
        if (SORT_PRICE_DESC.equals(sort)) return " ORDER BY IFNULL(promo_price, p.price) DESC, p.id DESC";
        if (SORT_SALES.equals(sort)) return " ORDER BY p.sales DESC, p.id DESC";
        return " ORDER BY p.id DESC";
    }

    @Override
    public Result<PageResult<ProductVO>> listProducts(Long categoryId, String keyword, String sort, int page, int size) {
        return execute(conn -> {
            StringBuilder where = new StringBuilder(" WHERE p.status = 1");
            List<Object> params = new ArrayList<>();
            if (categoryId != null) {
                where.append(" AND p.category_id = ?");
                params.add(categoryId);
            }
            String kw = like(keyword);
            if (kw != null) {
                where.append(" AND p.name LIKE ?");
                params.add(kw);
            }
            String countSql = "SELECT COUNT(*) FROM t_product p" + where;
            String baseSql = SELECT_PRODUCT + where + orderBy(sort);
            return queryPage(conn, countSql, baseSql, ProductDaoImpl::mapProduct, page, size, params.toArray());
        });
    }

    @Override
    public Result<ProductVO> getProductDetail(Long id) {
        return execute(conn -> {
            requireNotNull(id, "商品 ID");
            ProductVO vo = findById(conn, id);
            if (vo == null || !vo.isOnSale()) {
                throw new BizException(ResultCode.PRODUCT_OFF);
            }
            return vo;
        });
    }

    @Override
    public Result<List<ProductVO>> listHotProducts(int limit) {
        return execute(conn -> query(conn,
                SELECT_PRODUCT + "WHERE p.status = 1 ORDER BY p.sales DESC, p.id DESC LIMIT ?",
                ProductDaoImpl::mapProduct, limit <= 0 ? 10 : limit));
    }

    /** 新增 / 修改共用的入参校验 */
    private void validate(Connection conn, Product p) throws SQLException {
        requireNotNull(p, "商品");
        requireNotNull(p.getCategoryId(), "商品分类");
        requireNotBlank(p.getName(), "商品名称");
        if (!MoneyUtil.isPositive(p.getPrice())) {
            throw new BizException(ResultCode.BAD_REQUEST, "商品价格必须大于 0");
        }
        if (p.getStock() == null || p.getStock() < 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "库存不能小于 0");
        }
        long categoryExists = queryLong(conn, "SELECT COUNT(*) FROM t_category WHERE id = ?", p.getCategoryId());
        if (categoryExists == 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "商品分类不存在");
        }
    }

    @Override
    public Result<ProductVO> addProduct(Long adminId, Product product) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            validate(conn, product);
            int status = product.getStatus() == null ? 1 : product.getStatus();
            long id = insert(conn,
                    "INSERT INTO t_product(category_id, name, subtitle, main_image, detail, price, stock, sales, status, create_time) "
                            + "VALUES(?, ?, ?, ?, ?, ?, ?, 0, ?, NOW())",
                    product.getCategoryId(), product.getName().trim(), product.getSubtitle(), product.getMainImage(),
                    product.getDetail(), MoneyUtil.scale(product.getPrice()), product.getStock(), status);
            return findById(conn, id);
        });
    }

    @Override
    public Result<ProductVO> updateProduct(Long adminId, Product product) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            requireNotNull(product, "商品");
            requireNotNull(product.getId(), "商品 ID");
            validate(conn, product);
            ProductVO old = findById(conn, product.getId());
            if (old == null) throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
            int status = product.getStatus() == null ? old.getStatus() : product.getStatus();
            update(conn,
                    "UPDATE t_product SET category_id=?, name=?, subtitle=?, main_image=?, detail=?, price=?, stock=?, status=?, update_time=NOW() WHERE id=?",
                    product.getCategoryId(), product.getName().trim(), product.getSubtitle(), product.getMainImage(),
                    product.getDetail(), MoneyUtil.scale(product.getPrice()), product.getStock(), status, product.getId());
            return findById(conn, product.getId());
        });
    }

    @Override
    public Result<Void> updateProductStatus(Long adminId, Long id, Integer status) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            requireNotNull(id, "商品 ID");
            if (status == null || (status != 0 && status != 1)) {
                throw new BizException(ResultCode.BAD_REQUEST, "状态值不正确");
            }
            int rows = update(conn, "UPDATE t_product SET status=?, update_time=NOW() WHERE id=?", status, id);
            if (rows == 0) throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
            return null;
        });
    }

    @Override
    public Result<Void> deleteProduct(Long adminId, Long id) {
        return executeTx(conn -> {
            requireAdmin(conn, adminId);
            requireNotNull(id, "商品 ID");
            // 同步删除购物车中的该商品与相关活动
            update(conn, "DELETE FROM t_cart WHERE product_id = ?", id);
            update(conn, "DELETE FROM t_promotion WHERE product_id = ?", id);
            int rows = update(conn, "DELETE FROM t_product WHERE id = ?", id);
            if (rows == 0) throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
            return null;
        });
    }

    @Override
    public Result<PageResult<ProductVO>> listAllProducts(Long adminId, String keyword, int page, int size) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            String kw = like(keyword);
            String where = kw == null ? "" : " WHERE p.name LIKE ?";
            Object[] params = kw == null ? new Object[0] : new Object[]{kw};
            String countSql = "SELECT COUNT(*) FROM t_product p" + where;
            String baseSql = SELECT_PRODUCT + where + " ORDER BY p.id DESC";
            return queryPage(conn, countSql, baseSql, ProductDaoImpl::mapProduct, page, size, params);
        });
    }
}
