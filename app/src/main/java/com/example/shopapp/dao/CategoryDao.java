package com.example.shopapp.dao;

import com.example.shopapp.common.Result;
import com.example.shopapp.entity.Category;

import java.util.List;

/**
 * 商品分类模块（接口文档 3）。
 */
public interface CategoryDao {

    /** 3.1 分类列表：只返回 status=1 的分类，按 sort 升序 */
    Result<List<Category>> listCategories();

    /** 后台使用：返回全部分类（含停用），按 sort 升序 */
    Result<List<Category>> listAllCategories(Long adminId);

    /** 3.2 新增分类【管理员】 */
    Result<Category> addCategory(Long adminId, String name, String icon, Integer sort);

    /** 3.3 修改分类【管理员】 */
    Result<Category> updateCategory(Long adminId, Long id, String name, String icon, Integer sort, Integer status);

    /** 3.4 删除分类【管理员】：分类下存在商品时不允许删除，返回 400「该分类下存在商品」 */
    Result<Void> deleteCategory(Long adminId, Long id);
}
