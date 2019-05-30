package com.seckill.dis.user.redis;

/**
 * 访问次数的key前缀
 */
public class AccessKeyPrefix extends BaseKeyPrefix {
    public AccessKeyPrefix(String prefix) {
        super(prefix);
    }

    public AccessKeyPrefix(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    // 可灵活设置过期时间
    public static AccessKeyPrefix withExpire(int expireSeconds) {
        return new AccessKeyPrefix(expireSeconds, "access");
    }
}
