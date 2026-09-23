package com.example.shopapp.dao.impl;

import com.example.shopapp.common.BizException;
import com.example.shopapp.common.Result;
import com.example.shopapp.common.ResultCode;
import com.example.shopapp.dao.CategoryDao;
import com.example.shopapp.entity.Category;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 商品分类模块实现（接口文档 3）。
 */
public class CategoryDaoImpl extends BaseDao implements CategoryDao {

    static Category mapCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setIcon(rs.getString("icon"));
        c.setSort(getInt(rs, "sort"));
        c.setStatus(getInt(rs, "status"));
        return c;
    }

    private Category findById(Connection conn, Long id) throws SQLException {
        return queryOne(conn, "SELECT * FROM t_category WHERE id = ?", CategoryDaoImpl::mapCategory, id);
    }

    @Override
    public Result<List<Category>> listCategories() {
        return execute(conn -> query(conn,
                "SELECT * FROM t_category WHERE status = 1 ORDER BY sort ASC, id ASC",
                CategoryDaoImpl::mapCategory));
    }

    @Override
    public Result<List<Category>> listAllCategories(Long adminId) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            return query(conn, "SELECT * FROM t_category ORDER BY sort ASC, id ASC", CategoryDaoImpl::mapCategory);
        });
    }

    @Override
    public Result<Category> addCategory(Long adminId, String name, String icon, Integer sort) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            requireNotBlank(name, "分类名称");
            long id = insert(conn, "INSERT INTO t_category(name, icon, sort, status) VALUES(?, ?, ?, 1)",
                    name.trim(), icon, sort == null ? 0 : sort);
            return findById(conn, id);
        });
    }

    @Override
    public Result<Category> updateCategory(Long adminId, Long id, String name, String icon, Integer sort, Integer status) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            requireNotNull(id, "分类 ID");
            Category old = findById(conn, id);
            if (old == null) throw new BizException(ResultCode.NOT_FOUND, "分类不存在");
            // 传 null 的字段不修改
            update(conn, "UPDATE t_category SET name=?, icon=?, sort=?, status=? WHERE id=?",
                    name == null ? old.getName() : name.trim(),
                    icon == null ? old.getIcon() : icon,
                    sort == null ? old.getSort() : sort,
                    status == null ? old.getStatus() : status,
                    id);
            return findById(conn, id);
        });
    }

    @Override
    public Result<Void> deleteCategory(Long adminId, Long id) {
        return execute(conn -> {
            requireAdmin(conn, adminId);
            requireNotNull(id, "分类 ID");
            long count = queryLong(conn, "SELECT COUNT(*) FROM t_product WHERE category_id = ?", id);
            if (count > 0) {
                throw new BizException(ResultCode.BAD_REQUEST, "该分类下存在商品");
            }
            int rows = update(conn, "DELETE FROM t_category WHERE id = ?", id);
            if (rows == 0) throw new BizException(ResultCode.NOT_FOUND, "分类不存在");
            return null;
        });
    }
}
