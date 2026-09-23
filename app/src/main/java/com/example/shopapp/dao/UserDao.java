package com.example.shopapp.dao;

import com.example.shopapp.common.Result;
import com.example.shopapp.vo.LoginVO;
import com.example.shopapp.vo.UserVO;

/**
 * 用户模块（接口文档 2）。所有方法必须在子线程调用。
 */
public interface UserDao {

    /**
     * 2.1 用户注册：用户名唯一；密码 MD5 后写入；新用户 role=0、status=1。
     *
     * @param username 4~20 位字母数字
     * @param password 6~20 位
     * @param nickname 可空，默认等于用户名
     * @param phone    可空，11 位
     * @return UserVO；失败码 400、1001
     */
    Result<UserVO> register(String username, String password, String nickname, String phone);

    /**
     * 2.2 用户登录。
     *
     * @param password 明文密码，DAO 内部 MD5 后比对
     * @return LoginVO；失败码 1002、1003
     */
    Result<LoginVO> login(String username, String password);

    /** 2.3 获取个人信息。失败码 404 */
    Result<UserVO> getUserInfo(Long userId);

    /** 2.4 修改个人信息，传 null 的字段不修改 */
    Result<UserVO> updateUserInfo(Long userId, String nickname, String phone, String avatar);

    /** 2.5 修改密码。失败码 1004（原密码错误）、400（新密码格式） */
    Result<Void> updatePassword(Long userId, String oldPassword, String newPassword);

    /** 2.6 退出登录：客户端本地操作，清除 SessionManager 中的登录信息，不访问数据库 */
    void logout();
}
