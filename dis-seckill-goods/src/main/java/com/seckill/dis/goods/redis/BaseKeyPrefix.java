package com.seckill.dis.goods.redis;


/**
 * 模板方法的基本类
 * @author noodle
 */
public abstract class BaseKeyPrefix implements KeyPrefix {

    int expireSeconds;// 过期时间
    String prefix;// 前缀


    /**
     * 默认过期时间为0，即不过期，过期时间只收到redis的缓存策略影响
     *
     * @param prefix 前缀
     */
    public BaseKeyPrefix(String prefix) {
        this(0, prefix);
    }


    public BaseKeyPrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    /**
     * 默认0代表永不过期
     *
     * @return
     */
    @Override
    public int expireSeconds() {
        return expireSeconds;
    }

    /**
     * 前缀为模板类的实现类的类名
     *
     * @return
     */
    @Override
    public String getPrefix() {
        String simpleName = getClass().getSimpleName();
        return simpleName + ":" + prefix;
    }
}
