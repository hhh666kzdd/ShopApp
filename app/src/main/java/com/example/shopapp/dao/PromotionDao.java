package com.example.shopapp.dao;

import com.example.shopapp.common.PageResult;
import com.example.shopapp.common.Result;
import com.example.shopapp.entity.Promotion;
import com.example.shopapp.vo.PromotionVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 限时特价活动模块（接口文档 6.2）。
 */
public interface PromotionDao {

    /** 6.2.1 进行中的活动列表：status=1 且 start_time <= NOW() <= end_time */
    Result<List<PromotionVO>> listActivePromotions();

    /**
     * 6.2.2 查询商品现价：存在进行中的活动返回活动价，否则返回原价；
     * 购物车、下单统一调用此方法计价。商品不存在返回 null。
     */
    BigDecimal getEffectivePrice(Long productId);

    /** 6.2.3 新增活动【管理员】，活动价必须低于商品原价 */
    Result<PromotionVO> addPromotion(Long adminId, Promotion promotion);

    /** 6.2.3 修改活动【管理员】 */
    Result<PromotionVO> updatePromotion(Long adminId, Promotion promotion);

    /** 6.2.3 删除活动【管理员】 */
    Result<Void> deletePromotion(Long adminId, Long id);

    /** 6.2.3 后台活动列表【管理员】 */
    Result<PageResult<PromotionVO>> listAllPromotions(Long adminId, int page, int size);
}
