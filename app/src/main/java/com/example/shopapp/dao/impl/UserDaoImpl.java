package com.example.shopapp.dao.impl;

import com.example.shopapp.common.BizException;
import com.example.shopapp.common.Result;
import com.example.shopapp.common.ResultCode;
import com.example.shopapp.dao.UserDao;
import com.example.shopapp.util.MD5Util;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.util.ValidateUtil;
import com.example.shopapp.vo.LoginVO;
import com.example.shopapp.vo.UserVO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户模块实现（接口文档 2）。
 */
public class UserDaoImpl extends BaseDao implements UserDao {

    /** t_user 行 -> UserVO */
    static UserVO mapUser(ResultSet rs) throws SQLException {
        UserVO vo = new UserVO();
        vo.setId(rs.getLong("id"));
        vo.setUsername(rs.getString("username"));
        vo.setNickname(rs.getString("nickname"));
        vo.setPhone(rs.getString("phone"));
        vo.setAvatar(rs.getString("avatar"));
        vo.setRole(getInt(rs, "role"));
        vo.setStatus(getInt(rs, "status"));
        vo.setCreateTime(getTime(rs, "create_time"));
        return vo;
    }

    private UserVO findById(Connection conn, Long userId) throws SQLException {
        return queryOne(conn, "SELECT * FROM t_user WHERE id = ?", UserDaoImpl::mapUser, userId);
    }

    @Override
    public Result<UserVO> register(String username, String password, String nickname, String phone) {
        return execute(conn -> {
            // 参数校验
            if (!ValidateUtil.isUsername(username)) {
                throw new BizException(ResultCode.BAD_REQUEST, "用户名需为 4~20 位字母或数字");
            }
            if (!ValidateUtil.isPassword(password)) {
                throw new BizException(ResultCode.BAD_REQUEST, "密码需为 6~20 位");
            }
            if (!ValidateUtil.isPhoneOrEmpty(phone)) {
                throw new BizException(ResultCode.BAD_REQUEST, "手机号格式不正确");
            }
            // 用户名唯一
            long exists = queryLong(conn, "SELECT COUNT(*) FROM t_user WHERE username = ?", username);
            if (exists > 0) {
                throw new BizException(ResultCode.USER_EXISTS);
            }
            String nick = ValidateUtil.isEmpty(nickname) ? username : nickname.trim();
            String ph = ValidateUtil.isEmpty(phone) ? null : phone.trim();
            long id = insert(conn,
                    "INSERT INTO t_user(username, password, nickname, phone, role, status, create_time) "
                            + "VALUES(?, ?, ?, ?, 0, 1, NOW())",
                    username, MD5Util.md5(password), nick, ph);
            return findById(conn, id);
        });
    }

    @Override
    public Result<LoginVO> login(String username, String password) {
        return execute(conn -> {
            if (ValidateUtil.isEmpty(username) || ValidateUtil.isEmpty(password)) {
                throw new BizException(ResultCode.BAD_REQUEST, "请输入用户名和密码");
            }
            UserVO user = queryOne(conn, "SELECT * FROM t_user WHERE username = ? AND password = ?",
                    UserDaoImpl::mapUser, username.trim(), MD5Util.md5(password));
            if (user == null) {
                throw new BizException(ResultCode.LOGIN_FAILED);
            }
            if (!user.isEnabled()) {
                throw new BizException(ResultCode.USER_DISABLED);
            }
            LoginVO vo = new LoginVO();
            vo.setUserId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setRole(user.getRole());
            return vo;
        });
    }

    @Override
    public Result<UserVO> getUserInfo(Long userId) {
        return execute(conn -> {
            requireLogin(userId);
            UserVO user = findById(conn, userId);
            if (user == null) throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
            return user;
        });
    }

    @Override
    public Result<UserVO> updateUserInfo(Long userId, String nickname, String phone, String avatar) {
        return execute(conn -> {
            requireLogin(userId);
            UserVO old = findById(conn, userId);
            if (old == null) throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
            if (phone != null && !ValidateUtil.isPhoneOrEmpty(phone)) {
                throw new BizException(ResultCode.BAD_REQUEST, "手机号格式不正确");
            }
            // 传 null 的字段保持原值
            String newNick = nickname == null ? old.getNickname() : nickname.trim();
            String newPhone = phone == null ? old.getPhone() : (phone.trim().isEmpty() ? null : phone.trim());
            String newAvatar = avatar == null ? old.getAvatar() : avatar.trim();
            update(conn, "UPDATE t_user SET nickname=?, phone=?, avatar=?, update_time=NOW() WHERE id=?",
                    newNick, newPhone, newAvatar, userId);
            return findById(conn, userId);
        });
    }

    @Override
    public Result<Void> updatePassword(Long userId, String oldPassword, String newPassword) {
        return execute(conn -> {
            requireLogin(userId);
            if (!ValidateUtil.isPassword(newPassword)) {
                throw new BizException(ResultCode.BAD_REQUEST, "新密码需为 6~20 位");
            }
            long matched = queryLong(conn, "SELECT COUNT(*) FROM t_user WHERE id = ? AND password = ?",
                    userId, MD5Util.md5(oldPassword == null ? "" : oldPassword));
            if (matched == 0) {
                throw new BizException(ResultCode.OLD_PASSWORD_WRONG);
            }
            update(conn, "UPDATE t_user SET password=?, update_time=NOW() WHERE id=?",
                    MD5Util.md5(newPassword), userId);
            return null;
        });
    }

    @Override
    public void logout() {
        SessionManager.logout();
    }
}
