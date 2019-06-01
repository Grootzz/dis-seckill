package com.seckill.dis.cache.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis service封装对redis的操作，如get、set、incr等
 *
 * @author noodle
 */

@Service
public class RedisService {

    /**
     * 通过连接池对象可以获得对redis的连接
     */
    @Autowired
    JedisPool jedisPool;

    /**
     * redis 的get操作，通过key获取存储在redis中的对象
     *
     * @param prefix key的前缀
     * @param key    业务层传入的key
     * @param clazz  存储在redis中的对象类型（redis中是以字符串存储的）
     * @param <T>    指定对象对应的类型
     * @return 存储于redis中的对象
     */
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

    /**
     * redis的set操作
     *
     * @param prefix 键的前缀
     * @param key    键
     * @param value  值
     * @return 操作成功为true，否则为true
     */
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
            int seconds = prefix.expireSeconds();

            if (seconds <= 0) {
                // 设置key的过期时间为redis默认值（由redis的缓存策略控制）
                jedis.set(realKey, strValue);
            } else {
                // 设置key的过期时间
                jedis.setex(realKey, seconds, strValue);
            }

            return true;

        } finally {
            returnToPool(jedis);
        }
    }


    /**
     * 判断key是否存在于redis中
     *
     * @param keyPrefix key的前缀
     * @param key
     * @param <T>
     * @return
     */
    public <T> boolean exists(KeyPrefix keyPrefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = keyPrefix.getPrefix() + key;
            return jedis.exists(realKey);
        } finally {
            returnToPool(jedis);
        }
    }


    /**
     * 自增
     *
     * @param keyPrefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long incr(KeyPrefix keyPrefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = keyPrefix.getPrefix() + key;
            return jedis.incr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }


    /**
     * 自减
     *
     * @param keyPrefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long decr(KeyPrefix keyPrefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = keyPrefix.getPrefix() + key;
            return jedis.decr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 删除缓存中的用户数据
     *
     * @param prefix
     * @param key
     * @return
     */
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
