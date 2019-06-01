package com.seckill.dis.common.api.cache.vo;

import java.io.Serializable;

/**
 * 访问次数的key前缀
 *
 * @author noodle
 */
public class AccessKeyPrefix extends BaseKeyPrefix implements Serializable{
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
