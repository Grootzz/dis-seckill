package com.seckill.dis.gateway.config.access;

import com.alibaba.fastjson.JSON;
import com.seckill.dis.common.api.user.UserServiceApi;
import com.seckill.dis.common.api.user.vo.UserVo;
import com.seckill.dis.common.result.CodeMsg;
import com.seckill.dis.common.result.Result;
import com.seckill.dis.gateway.redis.AccessKeyPrefix;
import com.seckill.dis.gateway.redis.RedisService;
import com.seckill.dis.gateway.redis.SeckillUserKeyPrefix;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * 用户访问拦截器
 *
 * @author noodle
 */
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    private static Logger logger = LoggerFactory.getLogger(AccessInterceptor.class);

    @Reference(interfaceClass = UserServiceApi.class)
    UserServiceApi userService;

    @Autowired
    RedisService redisService;

    /**
     * 目标方法执行前的处理
     * <p>
     * 查询访问次数，进行防刷请求拦截
     * 在 AccessLimit#seconds() 时间内频繁访问会有次数限制
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info(request.getRequestURL() + " 拦截请求");

        // 指明拦截的是方法
        if (handler instanceof HandlerMethod) {
            logger.info("HandlerMethod: " + ((HandlerMethod) handler).getMethod().getName());

            UserVo user = this.getUser(request, response);// 获取用户对象

            UserContext.setUser(user); // 保存用户到ThreadLocal，这样，同一个线程访问的是同一个用户

            // 获取标注了 @AccessLimit 的方法，没有注解，则直接返回
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);

            // 如果没有添加@AccessLimit注解，直接放行（true）
            if (accessLimit == null)
                return true;

            // 获取注解的元素值
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxAccessCount();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    this.render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getPhone();
            } else {
                //do nothing
            }
            // 设置缓存过期时间
            AccessKeyPrefix accessKeyPrefix = AccessKeyPrefix.withExpire(seconds);
            // 在redis中存储的访问次数的key为请求的URI
            Integer count = redisService.get(accessKeyPrefix, key, Integer.class);
            // 第一次重复点击 秒杀按钮
            if (count == null) {
                redisService.set(accessKeyPrefix, key, 1);
                // 点击次数未达最大值
            } else if (count < maxCount) {
                redisService.incr(accessKeyPrefix, key);
            } else {
                // 点击次数已满
                this.render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        // 不是方法直接放行
        return true;
    }

    /**
     * 渲染返回信息
     * 以 json 格式返回
     *
     * @param response
     * @param cm
     * @throws Exception
     */
    private void render(HttpServletResponse response, CodeMsg cm) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    /**
     * 和 UserArgumentResolver 功能类似，用于解析拦截的请求，获取 UserVo 对象
     *
     * @param request
     * @param response
     * @return UserVo 对象
     */
    private UserVo getUser(HttpServletRequest request, HttpServletResponse response) {
        logger.info(request.getRequestURL() + " 获取 UserVo 对象");

        // 从请求中获取token
        String paramToken = request.getParameter(UserServiceApi.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, UserServiceApi.COOKIE_NAME_TOKEN);

        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }

        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;

        if (StringUtils.isEmpty(token)) {
            return null;
        }

        UserVo userVo = redisService.get(SeckillUserKeyPrefix.token, token, UserVo.class);

        // 在有效期内从redis获取到key之后，需要将key重新设置一下，从而达到延长有效期的效果
        if (userVo != null) {
            addCookie(response, token, userVo);
        }
        return userVo;
    }

    /**
     * 从众多的cookie中找出指定cookiName的cookie
     *
     * @param request
     * @param cookieName
     * @return cookiName对应的value
     */
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0)
            return null;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 将cookie存入redis，并将cookie写入到请求的响应中
     *
     * @param response
     * @param token
     * @param user
     */
    private void addCookie(HttpServletResponse response, String token, UserVo user) {

        redisService.set(SeckillUserKeyPrefix.token, token, user);

        Cookie cookie = new Cookie(UserServiceApi.COOKIE_NAME_TOKEN, token);
        // 客户端cookie的有限期和缓存中的cookie有效期一致
        cookie.setMaxAge(SeckillUserKeyPrefix.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
