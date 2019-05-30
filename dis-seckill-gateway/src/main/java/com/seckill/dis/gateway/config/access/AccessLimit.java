package com.seckill.dis.gateway.config.access;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 用户访问拦截的注解
 * 主要用于防止刷功能的实现
 *
 * @author noodle
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface AccessLimit {

    // 最大请求次数的时间间隔
    int seconds();

    // 最大请求次数
    int maxAccessCount();

    // 是否㤇重新登录
    boolean needLogin() default true;
}
