package com.example.shopapp.dao;

import com.example.shopapp.common.PageResult;
import com.example.shopapp.common.Result;
import com.example.shopapp.vo.StatisticsVO;
import com.example.shopapp.vo.UserVO;

/**
 * 后台管理模块【管理员】（接口文档 9）。
 */
public interface AdminDao {

    /** 9.1 用户列表：按用户名 / 昵称 / 手机号模糊搜索 */
    Result<PageResult<UserVO>> listUsers(Long adminId, String keyword, int page, int size);

    /** 9.2 启用 / 禁用用户：1 正常，0 禁用；不允许禁用管理员自己 */
    Result<Void> updateUserStatus(Long adminId, Long userId, Integer status);

    /** 9.3 重置用户密码为默认密码 123456（MD5 存储） */
    Result<Void> resetPassword(Long adminId, Long userId);

    /** 9.4 销售统计 */
    Result<StatisticsVO> getStatistics(Long adminId);
}
