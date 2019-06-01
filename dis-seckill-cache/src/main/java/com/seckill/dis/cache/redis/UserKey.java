package com.seckill.dis.cache.redis;

/**
 * redis中，用于管理用户表的key
 */
public class UserKey extends BaseKeyPrefix {

    public UserKey(String prefix) {
        super(prefix);
    }

    public static UserKey getById = new UserKey("id");
    public static UserKey getByName = new UserKey("name");

}
