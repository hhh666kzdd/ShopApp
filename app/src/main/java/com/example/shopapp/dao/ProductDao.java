package com.example.shopapp.dao;

import com.example.shopapp.common.PageResult;
import com.example.shopapp.common.Result;
import com.example.shopapp.entity.Product;
import com.example.shopapp.vo.ProductVO;

import java.util.List;

/**
 * 商品模块（接口文档 4）。
 */
public interface ProductDao {

    /** 排序方式常量（接口文档 4.1） */
    String SORT_DEFAULT = "default";
    String SORT_PRICE_ASC = "price_asc";
    String SORT_PRICE_DESC = "price_desc";
    String SORT_SALES = "sales";

    /**
     * 4.1 商品列表 / 搜索：只返回上架商品；每个商品填充 promoPrice。
     *
     * @param categoryId 分类 ID，null 表示全部
     * @param keyword    按商品名称模糊搜索，可空
     * @param sort       default / price_asc / price_desc / sales
     * @param page       页码，从 1 开始
     * @param size       每页条数
     */
    Result<PageResult<ProductVO>> listProducts(Long categoryId, String keyword, String sort, int page, int size);

    /** 4.2 商品详情。失败码 2001 */
    Result<ProductVO> getProductDetail(Long id);

    /** 4.3 热销商品：上架商品按 sales 降序取前 limit 条 */
    Result<List<ProductVO>> listHotProducts(int limit);

    /** 4.4 新增商品【管理员】 */
    Result<ProductVO> addProduct(Long adminId, Product product);

    /** 4.5 修改商品【管理员】，product.id 必填 */
    Result<ProductVO> updateProduct(Long adminId, Product product);

    /** 4.6 商品上下架【管理员】 */
    Result<Void> updateProductStatus(Long adminId, Long id, Integer status);

    /** 4.7 删除商品【管理员】：同步删除购物车中的该商品 */
    Result<Void> deleteProduct(Long adminId, Long id);

    /** 4.8 后台商品列表【管理员】：包含下架商品 */
    Result<PageResult<ProductVO>> listAllProducts(Long adminId, String keyword, int page, int size);
}
