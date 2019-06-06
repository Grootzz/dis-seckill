package com.seckill.dis.gateway.user;

import com.seckill.dis.common.api.cache.vo.SkUserKeyPrefix;
import com.seckill.dis.common.api.user.UserServiceApi;
import com.seckill.dis.common.api.user.vo.LoginVo;
import com.seckill.dis.common.result.Result;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 用户接口
 *
 * @author noodle
 */
@Controller
@RequestMapping("/user/")
public class UserController {
    /**
     * 日志记录：Logger是由slf4j接口规范创建的，对象有具体的实现类创建
     */
    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Reference(interfaceClass = UserServiceApi.class)
    UserServiceApi userService;

    /**
     * 由于需要将一个cookie对应的用户存入第三方缓存中，这里用redis，所以需要引入redis serice
     */
//    @Reference(interfaceClass = RedisServiceApi.class)
//    RedisServiceApi redisService;

    /**
     * 首页
     *
     * @return
     */
    @RequestMapping(value = "index", method = RequestMethod.GET)
    public String index() {
        logger.info("首页接口");
        return "login";// login页面
    }

    /**
     * 用户登录接口
     *
     * @param response 响应
     * @param loginVo  用户登录请求的表单数据（将表单数据封装为了一个Vo：Value Object）
     *                 注解@Valid用于校验表单参数，校验成功才会继续执行业务逻辑，否则，
     *                 请求参数校验不成功抛出异常
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> login(HttpServletResponse response, @Valid LoginVo loginVo) {
//        // 打印接收的表单数据
//        logger.info(loginVo.toString());
//
//        // 抛出的异常信息会被全局异常接收，全局异常会将异常信息传递到全局异常处理器
//        if (loginVo == null)
//            throw new GlobalException(CodeMsg.SERVER_ERROR);
//
//        // 获取用户提交的手机号码和密码
//        String phone = loginVo.getMobile();
//        String password = loginVo.getPassword();
//
//        // 判断手机号是否存在(首先从缓存中取，再从数据库取)
//        UserVo user = this.getUserVo(Long.parseLong(phone));
//        if (user == null)
//            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
//        logger.info("用户：" + user.toString());
//
//        // 执行到这里表明登录成功，更新用户cookie
//        String dbPassword = user.getPassword();
//        String dbSalt = user.getSalt();
//        String calcPass = MD5Util.formPassToDbPass(password, dbSalt);
//        if (!calcPass.equals(dbPassword))
//            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
//
//        // 执行到这里表明登录成功了
//        // 生成cookie
//        String token = UUIDUtil.uuid();
//        // 每次访问都会生成一个新的session存储于redis和反馈给客户端，一个session对应存储一个user对象
//        redisService.set(SeckillUserKeyPrefix.token, token, user);
//        // 将token写入cookie中, 然后传给客户端（一个cookie对应一个用户，这里将这个cookie的用户信息写入redis中）
//        Cookie cookie = new Cookie(UserServiceApi.COOKIE_NAME_TOKEN, token);
//        // 保持与redis中的session一致
//        cookie.setMaxAge(SeckillUserKeyPrefix.token.expireSeconds());
//        cookie.setPath("/");
//        response.addCookie(cookie);
//
//        logger.info(cookie.getName() + ": " + cookie.getValue());

//        String token = userService.login(response, loginVo);
        String token = userService.login(loginVo);
        logger.info("token: " + token);

        // 将token写入cookie中, 然后传给客户端（一个cookie对应一个用户，这里将这个cookie的用户信息写入redis中）
        Cookie cookie = new Cookie(UserServiceApi.COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(SkUserKeyPrefix.TOKEN.expireSeconds());// 保持与redis中的session一致
        cookie.setPath("/");
        response.addCookie(cookie);
        // 返回登陆成功
        return Result.success(true);
    }

    public void register() {
        return;
    }


//    /**
//     * 根据 phone 查询秒杀用户信息
//     * <p>
//     * 对象级缓存
//     * 从缓存中查询 UserVo 对象，如果 UserVo 在缓存中存在，则直接返回，否则从数据库返回
//     *
//     * @param phone
//     * @return
//     */
//    private UserVo getUserVo(long phone) {
//
//        // 1. 从redis中获取用户数据缓存
//        UserVo user = redisService.get(SeckillUserKeyPrefix.getSeckillUserById, "" + phone, UserVo.class);
//        if (user != null)
//            return user;
//
//        // 2. 如果缓存中没有用户数据，则将数据写入缓存
//        // 先从数据库中取出数据
//        user = userService.getUserByPhone(phone);
//        // 然后将数据返回并将数据缓存在redis中
//        if (user != null)
//            redisService.set(SeckillUserKeyPrefix.getSeckillUserById, "" + phone, user);
//        return user;
//    }
}
