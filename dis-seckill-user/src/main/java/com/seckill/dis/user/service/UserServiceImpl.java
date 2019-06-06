package com.seckill.dis.user.service;

import com.seckill.dis.common.api.cache.RedisServiceApi;
import com.seckill.dis.common.api.cache.vo.SkUserKeyPrefix;
import com.seckill.dis.common.api.user.UserServiceApi;
import com.seckill.dis.common.api.user.vo.LoginVo;
import com.seckill.dis.common.api.user.vo.UserInfoVo;
import com.seckill.dis.common.api.user.vo.UserVo;
import com.seckill.dis.common.exception.GlobalException;
import com.seckill.dis.common.result.CodeMsg;
import com.seckill.dis.common.util.MD5Util;
import com.seckill.dis.common.util.UUIDUtil;
import com.seckill.dis.user.domain.SeckillUser;
import com.seckill.dis.user.persistence.SeckillUserMapper;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Service(interfaceClass = UserServiceApi.class)
public class UserServiceImpl implements UserServiceApi {

    private static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    SeckillUserMapper userMapper;

    /**
     * 由于需要将一个cookie对应的用户存入第三方缓存中，这里用redis，所以需要引入redis serice
     */
    @Reference(interfaceClass = RedisServiceApi.class)
    RedisServiceApi redisService;

    @Override
    public int login(String username, String password) {
        return 45;
    }

    @Override
    public boolean register(UserVo userModel) {
        return false;
    }

    @Override
    public boolean checkUsername(String username) {
        return false;
    }

    @Override
    public UserInfoVo getUserInfo(int uuid) {
        return null;
    }

    @Override
    public UserInfoVo updateUserInfo(UserInfoVo userInfoVo) {
        return null;
    }

    @Override
    public String login(@Valid LoginVo loginVo) {
        logger.info(loginVo.toString());

        // 抛出的异常信息会被全局异常接收，全局异常会将异常信息传递到全局异常处理器
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }

        // 获取用户提交的手机号码和密码
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        // 判断手机号是否存在(首先从缓存中取，再从数据库取)
        SeckillUser user = this.getSeckillUserById(Long.parseLong(mobile));
        // 缓存中、数据库中都不存在该用户信息，直接返回
        if (user == null)
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        logger.info("用户：" + user.toString());

        // 判断手机号对应的密码是否一致
        String dbPassword = user.getPassword();
        String dbSalt = user.getSalt();
        String calcPass = MD5Util.formPassToDbPass(password, dbSalt);
        if (!calcPass.equals(dbPassword))
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);

        // 执行到这里表明登录成功，更新用户cookie
        // 生成cookie
        String token = UUIDUtil.uuid();
        // 每次访问都会生成一个新的session存储于redis和反馈给客户端，一个session对应存储一个user对象
        redisService.set(SkUserKeyPrefix.TOKEN, token, user);
        return token;
    }

    /**
     * 用户登录, 要么处理成功返回true，否则会抛出全局异常
     * 抛出的异常信息会被全局异常接收，全局异常会将异常信息传递到全局异常处理器
     *
     * @param response
     * @param loginVo  封装了客户端请求传递过来的数据（即账号密码）
     *                 （使用post方式，请求参数放在了请求体中，这个参数就是获取请求体中的数据）
     * @return
     */
    @Override
    public String login(HttpServletResponse response, LoginVo loginVo) {

//        logger.info(loginVo.toString());
//
//        // 抛出的异常信息会被全局异常接收，全局异常会将异常信息传递到全局异常处理器
//        if (loginVo == null) {
//            throw new GlobalException(CodeMsg.SERVER_ERROR);
//        }
//
//        // 获取用户提交的手机号码和密码
//        String mobile = loginVo.getMobile();
//        String password = loginVo.getPassword();
//
//        // 判断手机号是否存在(首先从缓存中取，再从数据库取)
//        SeckillUser user = this.getSeckillUserById(Long.parseLong(mobile));
//        // 缓存中、数据库中都不存在该用户信息，直接返回
//        if (user == null)
//            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
//        logger.info("用户：" + user.toString());
//
//        // 判断手机号对应的密码是否一致
//        String dbPassword = user.getPassword();
//        String dbSalt = user.getSalt();
//        String calcPass = MD5Util.formPassToDbPass(password, dbSalt);
//        if (!calcPass.equals(dbPassword))
//            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
//
//        // 执行到这里表明登录成功，更新用户cookie
//        // 生成cookie
//        String token = UUIDUtil.uuid();
//        // 每次访问都会生成一个新的session存储于redis和反馈给客户端，一个session对应存储一个user对象
//        redisService.set(SkUserKeyPrefix.TOKEN, token, user);
//        // 将token写入cookie中, 然后传给客户端（一个cookie对应一个用户，这里将这个cookie的用户信息写入redis中）
//        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
//        cookie.setMaxAge(SkUserKeyPrefix.TOKEN.expireSeconds());// 保持与redis中的session一致
//        cookie.setPath("/");
//        response.addCookie(cookie);
//
//        return token;

        return null;
    }


    @Override
    public UserVo getUserByPhone(long phone) {
        UserVo userVo = new UserVo();
        SeckillUser user = userMapper.getUserByPhone(phone);

        userVo.setUuid(user.getUuid());
        userVo.setSalt(user.getSalt());
        userVo.setRegisterDate(user.getRegisterDate());
        userVo.setPhone(user.getPhone());
        userVo.setPassword(user.getPassword());
        userVo.setNickname(user.getNickname());
        userVo.setLoginCount(user.getLoginCount());
        userVo.setLastLoginDate(user.getLastLoginDate());
        userVo.setHead(user.getHead());

        return userVo;
    }

    /**
     * 根据 phone 查询秒杀用户信息
     * <p>
     * 对象级缓存
     * 从缓存中查询 SeckillUser 对象，如果 SeckillUser 在缓存中存在，则直接返回，否则从数据库返回
     *
     * @param phone
     * @return
     */
    private SeckillUser getSeckillUserById(long phone) {

        // 1. 从redis中获取用户数据缓存
        SeckillUser user = redisService.get(SkUserKeyPrefix.SK_USER_PHONE, "_" + phone, SeckillUser.class);

        if (user != null)
            return user;

        // 2. 如果缓存中没有用户数据，则将数据写入缓存
        // 先从数据库中取出数据
        user = userMapper.getUserByPhone(phone);
        // 然后将数据返回并将数据缓存在redis中
        if (user != null)
            redisService.set(SkUserKeyPrefix.SK_USER_PHONE, "_" + phone, user);
        return user;
    }
}
