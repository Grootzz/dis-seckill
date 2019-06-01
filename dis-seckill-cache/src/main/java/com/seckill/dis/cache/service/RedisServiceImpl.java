package com.seckill.dis.cache.service;

import com.alibaba.fastjson.JSON;
import com.seckill.dis.common.api.cache.RedisServiceApi;
import com.seckill.dis.common.api.cache.vo.KeyPrefix;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis服务实现
 *
 * @author noodle
 */
@Service(interfaceClass = RedisServiceApi.class)
public class RedisServiceImpl implements RedisServiceApi {

    /**
     * 通过连接池对象可以获得对redis的连接
     */
    @Autowired
    JedisPool jedisPool;

    @Override
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;// redis连接

        try {
            jedis = jedisPool.getResource();
            // 生成真正的存储于redis中的key
            String realKey = prefix.getPrefix() + key;
            // 通过key获取存储于redis中的对象（这个对象是以json格式存储的，所以是字符串）
            String strValue = jedis.get(realKey);
            // 将json字符串转换为对应的对象
            T objValue = stringToBean(strValue, clazz);
            return objValue;
        } finally {
            // 归还redis连接到连接池
            returnToPool(jedis);
        }
    }

    @Override
    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 将对象转换为json字符串
            String strValue = beanToString(value);

            if (strValue == null || strValue.length() <= 0)
                return false;

            // 生成实际存储于redis中的key
            String realKey = prefix.getPrefix() + key;
            // 获取key的过期时间
            int expires = prefix.expireSeconds();

            if (expires <= 0) {
                // 设置key的过期时间为redis默认值（由redis的缓存策略控制）
                jedis.set(realKey, strValue);
            } else {
                // 设置key的过期时间
                jedis.setex(realKey, expires, strValue);
            }
            return true;
        } finally {
            returnToPool(jedis);
        }
    }

    @Override
    public boolean exists(KeyPrefix keyPrefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = keyPrefix.getPrefix() + key;
            return jedis.exists(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    @Override
    public long incr(KeyPrefix keyPrefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = keyPrefix.getPrefix() + key;
            return jedis.incr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    @Override
    public long decr(KeyPrefix keyPrefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = keyPrefix.getPrefix() + key;
            return jedis.decr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    @Override
    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            Long del = jedis.del(realKey);
            return del > 0;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 将对象转换为对应的json字符串
     *
     * @param value 对象
     * @param <T>   对象的类型
     * @return 对象对应的json字符串
     */
    public static <T> String beanToString(T value) {
        if (value == null)
            return null;

        Class<?> clazz = value.getClass();
        /*首先对基本类型处理*/
        if (clazz == int.class || clazz == Integer.class)
            return "" + value;
        else if (clazz == long.class || clazz == Long.class)
            return "" + value;
        else if (clazz == String.class)
            return (String) value;
            /*然后对Object类型的对象处理*/
        else
            return JSON.toJSONString(value);
    }

    /**
     * 根据传入的class参数，将json字符串转换为对应类型的对象
     *
     * @param strValue json字符串
     * @param clazz    类型
     * @param <T>      类型参数
     * @return json字符串对应的对象
     */
    public static <T> T stringToBean(String strValue, Class<T> clazz) {

        if ((strValue == null) || (strValue.length() <= 0) || (clazz == null))
            return null;

        // int or Integer
        if ((clazz == int.class) || (clazz == Integer.class))
            return (T) Integer.valueOf(strValue);
            // long or Long
        else if ((clazz == long.class) || (clazz == Long.class))
            return (T) Long.valueOf(strValue);
            // String
        else if (clazz == String.class)
            return (T) strValue;
            // 对象类型
        else
            return JSON.toJavaObject(JSON.parseObject(strValue), clazz);
    }

    /**
     * 将redis连接对象归还到redis连接池
     *
     * @param jedis
     */
    private void returnToPool(Jedis jedis) {
        if (jedis != null)
            jedis.close();
    }

}
