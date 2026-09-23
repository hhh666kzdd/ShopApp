package com.example.shopapp.dao.impl;

import android.util.Log;

import com.example.shopapp.common.BizException;
import com.example.shopapp.common.PageResult;
import com.example.shopapp.common.Result;
import com.example.shopapp.common.ResultCode;
import com.example.shopapp.dao.PromotionDao;
import com.example.shopapp.entity.Promotion;
import com.example.shopapp.util.DBUtil;
import com.example.shopapp.util.DateUtil;
import com.example.shopapp.util.MoneyUtil;
import com.example.shopapp.vo.PromotionVO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * 限时特价活动模块实现（接口文档 6.2）。
 */
public class PromotionDaoImpl extends BaseDao implements PromotionDao {

    private static final String TAG = "PromotionDao";

    /** 活动联查商品名称、主图、原价 */
    private static final String SELECT_PROMOTION =
            "SELECT pm.*, p.name AS product_name, p.main_image AS product_image, p.price AS original_price "
                    + "FROM t_promotion pm JOIN t_product p ON pm.product_id = p.id ";

    static PromotionVO mapPromotion(ResultSet rs) throws SQLException {
        PromotionVO vo = new PromotionVO();
        vo.setId(rs.getLong("id"));
        vo.setTitle(rs.getString("title"));
        vo.setDescription(rs.getString("description"));
        vo.setProductId(rs.getLong("product_id"));
        vo.setPromoPrice(rs.getBigDecimal("promo_price"));
        vo.setStartTime(getTime(rs, "start_time"));
        vo.setEndTime(getTime(rs, "end_time"));
        vo.setStatus(getInt(rs, "status"));
        vo.setProductName(rs.getString("product_name"));
        vo.setProductImage(rs.getString("product_image"));
        vo.setOriginalPrice(rs.getBigDecimal("original_price"));
        return vo;
    }

    private PromotionVO findById(Connection conn, Long id) throws SQLException {
        return queryOne(conn, SELECT_PROMOTION + "WHERE pm.id = ?", PromotionDaoImpl::mapPromotion, id);
    }

    /**
     * 供购物车、订单模块在同一连接中复用：商品现价，商品不存在返回 null。
     */
    static BigDecimal effectivePrice(Connection conn, Long productId) throws SQLException {
        PromotionDaoImpl dao = new PromotionDaoImpl();
        BigDecimal promo = dao.queryOne(conn,
                "SELECT promo_price FROM t_promotion WHERE product_id=? AND status=1 AND start_time<=NOW() AND end_time>=NOW() "
                        + "ORDER BY promo_price ASC LIMIT 1",
                rs -> rs.getBigDecimal("promo_price"), productId);
        if (promo != null) return promo;
        return dao.queryOne(conn, "SELECT price FROM t_product WHERE id = ?", rs -> rs.getBigDecimal("price"), productId);
    }

    @Override
    public Result<List<PromotionVO>> listActivePromotions() {
        return execute(conn -> query(conn,
                SELECT_PROMOTION + "WHERE pm.status = 1 AND pm.start_time <= NOW() AND pm.end_time >= NOW() AND p.status = 1 "
                        + "ORDER BY pm.end_time ASC, pm.id DESC",
                PromotionDaoImpl::mapPromotion));
    }

    @Override
    public BigDecimal getEffectivePrice(Long productId) {
        if (productId == null) return null;
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return effectivePrice(conn, productId);
        } catch (SQLException e) {
            Log.e(TAG, "查询商品现价失败", e);
            return null;
        } finally {
            DBUtil.close(conn);
        }
    }

    /** 新增 / 修改共用校验：活动价必须低于商品原价，时间格式正确 */
    private void validate(Connection conn, Promotion p) throws SQLException {
        requireNotNull(p, "活动");
        requireNotBlank(p.getTitle(), "活动标题");
        requireNotNull(p.getProductId(), "活动商品");
        BigDecimal originalPrice = queryOne(conn, "SELECT price FROM t_product WHERE id = ?",
                rs -> rs.getBigDecimal("price"), p.getProductId());
        if (originalPrice == null) throw new BizException(ResultCode.PRODUCT_OFF, "活动商品不存在");
        if (!MoneyUtil.isPositive(p.getPromoPrice())) {
            throw new BizException(ResultCode.BAD_REQUEST, "活动价必须大于 0");
        }
        if (p.getPromoPrice().compareTo(originalPrice) >= 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "活动价必须低于商品原价 " + MoneyUtil.plain(originalPrice));
        }
        Timestamp start = DateUtil.parse(p.getStartTime());
        Timestamp end = DateUtil.parse(p.getEndTime());
        if (start == null || end == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "时间格式应为 yyyy-MM-dd HH:mm:ss");
        }
        if (!end.after(start)) {
            throw new BizException(ResultCode.BAD_REQUEST, "结束时间必须晚于开始时间");
        }
    }

    @Override
    public Result<PromotionVO> addPromotion(Long adminId, Promotion promotion) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            validate(conn, promotion);
            long id = insert(conn,
                    "INSERT INTO t_promotion(title, description, product_id, promo_price, start_time, end_time, status, create_time) "
                            + "VALUES(?, ?, ?, ?, ?, ?, ?, NOW())",
                    promotion.getTitle().trim(), promotion.getDescription(), promotion.getProductId(),
                    MoneyUtil.scale(promotion.getPromoPrice()),
                    DateUtil.parse(promotion.getStartTime()), DateUtil.parse(promotion.getEndTime()),
                    promotion.getStatus() == null ? 1 : promotion.getStatus());
            return findById(conn, id);
        });
    }

    @Override
    public Result<PromotionVO> updatePromotion(Long adminId, Promotion promotion) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            requireNotNull(promotion, "活动");
            requireNotNull(promotion.getId(), "活动 ID");
            validate(conn, promotion);
            PromotionVO old = findById(conn, promotion.getId());
            if (old == null) throw new BizException(ResultCode.NOT_FOUND, "活动不存在");
            update(conn,
                    "UPDATE t_promotion SET title=?, description=?, product_id=?, promo_price=?, start_time=?, end_time=?, status=? WHERE id=?",
                    promotion.getTitle().trim(), promotion.getDescription(), promotion.getProductId(),
                    MoneyUtil.scale(promotion.getPromoPrice()),
                    DateUtil.parse(promotion.getStartTime()), DateUtil.parse(promotion.getEndTime()),
                    promotion.getStatus() == null ? old.getStatus() : promotion.getStatus(),
                    promotion.getId());
            return findById(conn, promotion.getId());
        });
    }

    @Override
    public Result<Void> deletePromotion(Long adminId, Long id) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            requireNotNull(id, "活动 ID");
            int rows = update(conn, "DELETE FROM t_promotion WHERE id = ?", id);
            if (rows == 0) throw new BizException(ResultCode.NOT_FOUND, "活动不存在");
            return null;
        });
    }

    @Override
    public Result<PageResult<PromotionVO>> listAllPromotions(Long adminId, int page, int size) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            return queryPage(conn, "SELECT COUNT(*) FROM t_promotion pm JOIN t_product p ON pm.product_id = p.id",
                    SELECT_PROMOTION + "ORDER BY pm.id DESC", PromotionDaoImpl::mapPromotion, page, size);
        });
    }
}
