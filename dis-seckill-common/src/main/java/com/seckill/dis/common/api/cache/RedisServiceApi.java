package com.seckill.dis.common.api.cache;

import com.seckill.dis.common.api.cache.vo.KeyPrefix;

/**
 * redis 服务接口
 *
 * @author noodle
 */
public interface RedisServiceApi {


    /**
     * redis 的get操作，通过key获取存储在redis中的对象
     *
     * @param prefix key的前缀
     * @param key    业务层传入的key
     * @param clazz  存储在redis中的对象类型（redis中是以字符串存储的）
     * @param <T>    指定对象对应的类型
     * @return 存储于redis中的对象
     */
    <T> T get(KeyPrefix prefix, String key, Class<T> clazz);

    /**
     * redis的set操作
     *
     * @param prefix 键的前缀
     * @param key    键
     * @param value  值
     * @return 操作成功为true，否则为true
     */
    <T> boolean set(KeyPrefix prefix, String key, T value);

    /**
     * 判断key是否存在于redis中
     *
     * @param keyPrefix key的前缀
     * @param key
     * @return
     */
    boolean exists(KeyPrefix keyPrefix, String key);

    /**
     * 自增
     *
     * @param keyPrefix
     * @param key
     * @return
     */
    long incr(KeyPrefix keyPrefix, String key);

    /**
     * 自减
     *
     * @param keyPrefix
     * @param key
     * @return
     */
    long decr(KeyPrefix keyPrefix, String key);


    /**
     * 删除缓存中的用户数据
     *
     * @param prefix
     * @param key
     * @return
     */
    boolean delete(KeyPrefix prefix, String key);
}
