package com.seckill.dis.cache.redis;

/**
 * 存储订单的key前缀
 *
 * @author noodle
 */
public class OrderKeyPrefix extends BaseKeyPrefix {

    public OrderKeyPrefix(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public OrderKeyPrefix(String prefix) {
        super(prefix);
    }

    // 秒杀订单信息的前缀
    public static OrderKeyPrefix getSeckillOrderByUidGid = new OrderKeyPrefix("getSeckillOrderByUidGid");

}
