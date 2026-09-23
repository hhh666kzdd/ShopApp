package com.example.shopapp.dao.impl;

import android.util.Log;

import com.example.shopapp.common.BizException;
import com.example.shopapp.common.Result;
import com.example.shopapp.common.ResultCode;
import com.example.shopapp.util.DBUtil;
import com.example.shopapp.util.DateUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO 基类：封装连接获取、事务、异常到 Result 的转换以及 JDBC 常用操作。
 * <p>
 * 子类把业务逻辑写在 {@link SqlAction} 中，通过 {@link #execute} / {@link #executeTx} 执行：
 * 业务校验失败抛 {@link BizException}（转为对应错误码），SQL 异常统一转为 500。
 */
public abstract class BaseDao {

    private static final String TAG = "BaseDao";

    /** 数据库操作回调 */
    protected interface SqlAction<T> {
        T run(Connection conn) throws Exception;
    }

    /** 行映射器：把 ResultSet 当前行转换为对象 */
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    // ------------------------------------------------------------------
    // 执行框架
    // ------------------------------------------------------------------

    /** 在自动提交模式下执行（查询、单条更新） */
    protected <T> Result<T> execute(SqlAction<T> action) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return Result.ok(action.run(conn));
        } catch (BizException e) {
            return Result.fail(e.getCode(), e.getMessage());
        } catch (SQLException e) {
            Log.e(TAG, "SQL 异常", e);
            return Result.fail(ResultCode.SERVER_ERROR, "数据库异常：" + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "系统异常", e);
            return Result.fail(ResultCode.SERVER_ERROR, "系统异常：" + e.getMessage());
        } finally {
            DBUtil.close(conn);
        }
    }

    /** 在事务中执行：正常返回则提交，任何异常回滚 */
    protected <T> Result<T> executeTx(SqlAction<T> action) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            T data = action.run(conn);
            conn.commit();
            return Result.ok(data);
        } catch (BizException e) {
            rollback(conn);
            return Result.fail(e.getCode(), e.getMessage());
        } catch (SQLException e) {
            rollback(conn);
            Log.e(TAG, "SQL 异常（事务已回滚）", e);
            return Result.fail(ResultCode.SERVER_ERROR, "数据库异常：" + e.getMessage());
        } catch (Exception e) {
            rollback(conn);
            Log.e(TAG, "系统异常（事务已回滚）", e);
            return Result.fail(ResultCode.SERVER_ERROR, "系统异常：" + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
            DBUtil.close(conn);
        }
    }

    private static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    // ------------------------------------------------------------------
    // 权限 / 参数校验
    // ------------------------------------------------------------------

    /** 校验 adminId 对应用户为管理员，否则抛 403 */
    protected void requireAdmin(Connection conn, Long adminId) throws SQLException {
        if (adminId == null) throw new BizException(ResultCode.FORBIDDEN);
        Integer role = queryOne(conn, "SELECT role FROM t_user WHERE id = ? AND status = 1",
                rs -> rs.getInt("role"), adminId);
        if (role == null || role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    /** userId 为空视为未登录 */
    protected void requireLogin(Long userId) {
        if (userId == null || userId <= 0) throw new BizException(ResultCode.UNAUTHORIZED);
    }

    /** 参数为空时抛 400 */
    protected void requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, fieldName + "不能为空");
        }
    }

    protected void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new BizException(ResultCode.BAD_REQUEST, fieldName + "不能为空");
        }
    }

    // ------------------------------------------------------------------
    // JDBC 便捷方法
    // ------------------------------------------------------------------

    /** 依次设置占位符参数，支持 null、BigDecimal、Timestamp 等 */
    protected static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p == null) {
                ps.setNull(i + 1, java.sql.Types.NULL);
            } else if (p instanceof BigDecimal) {
                ps.setBigDecimal(i + 1, (BigDecimal) p);
            } else if (p instanceof Timestamp) {
                ps.setTimestamp(i + 1, (Timestamp) p);
            } else {
                ps.setObject(i + 1, p);
            }
        }
    }

    /** 查询列表 */
    protected <T> List<T> query(Connection conn, String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
            }
        }
        return list;
    }

    /** 查询单条，不存在返回 null */
    protected <T> T queryOne(Connection conn, String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper.map(rs) : null;
            }
        }
    }

    /** 查询单个整数值（COUNT 等），无结果返回 0 */
    protected long queryLong(Connection conn, String sql, Object... params) throws SQLException {
        Long v = queryOne(conn, sql, rs -> rs.getLong(1), params);
        return v == null ? 0 : v;
    }

    /** 查询单个金额值，无结果返回 0 */
    protected BigDecimal queryDecimal(Connection conn, String sql, Object... params) throws SQLException {
        BigDecimal v = queryOne(conn, sql, rs -> rs.getBigDecimal(1), params);
        return v == null ? BigDecimal.ZERO : v;
    }

    /** 执行 INSERT / UPDATE / DELETE，返回影响行数 */
    protected int update(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        }
    }

    /** 执行 INSERT 并返回自增主键 */
    protected long insert(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new SQLException("获取自增主键失败");
    }

    /**
     * 通用分页查询：baseSql 为不含 LIMIT 的 SELECT，countSql 为对应的 COUNT 语句。
     */
    protected <T> com.example.shopapp.common.PageResult<T> queryPage(
            Connection conn, String countSql, String baseSql, RowMapper<T> mapper,
            int page, int size, Object... params) throws SQLException {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        long total = queryLong(conn, countSql, params);
        Object[] pageParams = new Object[params.length + 2];
        System.arraycopy(params, 0, pageParams, 0, params.length);
        pageParams[params.length] = (page - 1) * size;
        pageParams[params.length + 1] = size;
        List<T> records = query(conn, baseSql + " LIMIT ?, ?", mapper, pageParams);
        return new com.example.shopapp.common.PageResult<>(records, total, page, size);
    }

    // ------------------------------------------------------------------
    // ResultSet 取值（区分 null）
    // ------------------------------------------------------------------

    protected static Long getLong(ResultSet rs, String col) throws SQLException {
        long v = rs.getLong(col);
        return rs.wasNull() ? null : v;
    }

    protected static Integer getInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    protected static String getTime(ResultSet rs, String col) throws SQLException {
        return DateUtil.format(rs.getTimestamp(col));
    }

    /** LIKE 模糊搜索参数：null / 空串返回 null 表示不筛选 */
    protected static String like(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return null;
        return "%" + keyword.trim() + "%";
    }
}
