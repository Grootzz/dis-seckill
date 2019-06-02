package com.seckill.dis.common.api.user;

import com.seckill.dis.common.api.user.vo.LoginVo;
import com.seckill.dis.common.api.user.vo.UserInfoVo;
import com.seckill.dis.common.api.user.vo.UserVo;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 用于用户交互api
 *
 * @author noodle
 */
public interface UserServiceApi {

    String COOKIE_NAME_TOKEN = "token";

    /**
     * 登录
     * 返回用户id；用户登录成功后，将用户id从后台传到前台，通过JWT的形式传到客户端，
     * 用户再次访问的时候，就携带JWT到服务端，服务端用一个缓存（如redis）将JWT缓存起来，
     * 并设置有效期，这样，用户不用每次访问都到数据库中查询用户id
     *
     * @param username
     * @param password
     * @return 用户id
     */
    int login(String username, String password);

    /**
     * 注册
     *
     * @param userModel
     * @return
     */
    boolean register(UserVo userModel);


    /**
     * 检查用户名是否存在
     *
     * @param username
     * @return
     */
    boolean checkUsername(String username);

    /**
     * 获取用户信息
     *
     * @param uuid
     * @return
     */
    UserInfoVo getUserInfo(int uuid);

    /**
     * 更新用户信息
     *
     * @param userInfoVo
     * @return
     */
    UserInfoVo updateUserInfo(UserInfoVo userInfoVo);

    /**
     * 登录
     *
     * @param loginVo
     * @return
     */
    String login(@Valid LoginVo loginVo);

    /**
     * 登录
     *
     * @param response
     * @param loginVo
     * @return
     */
    String login(HttpServletResponse response, @Valid LoginVo loginVo);

    /**
     * 根据phone获取用户
     *
     * @param phone
     * @return
     */
    UserVo getUserByPhone(long phone);
}
