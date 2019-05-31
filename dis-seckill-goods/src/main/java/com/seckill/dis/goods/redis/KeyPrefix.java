package com.seckill.dis.goods.redis;

/**
 * redis 键的前缀
 * 之所以在key前面设置一个前缀，是因为如果出现设置相同的key情形，可以通过前缀加以区分
 * 定义为接口是为了给模板方法提供一套规范
 *
 * @author noodle
 */
public interface KeyPrefix {

    /**
     * key的过期时间
     *
     * @return 过期时间
     */
    int expireSeconds();

    /**
     * key的前缀
     *
     * @return 前缀
     */
    String getPrefix();
}
