package com.seckill.dis.goods.redis;

/**
 * 秒杀用户信息的key前缀
 */

public class SeckillUserKeyPrefix extends BaseKeyPrefix {


    public static final int TOKEN_EXPIRE = 30*60;// 缓存有效时间为30min

    public SeckillUserKeyPrefix(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillUserKeyPrefix token = new SeckillUserKeyPrefix(TOKEN_EXPIRE, "token");
    // 用于存储用户对象到redis的key前缀
    public static SeckillUserKeyPrefix getSeckillUserById = new SeckillUserKeyPrefix(0, "id");

}
