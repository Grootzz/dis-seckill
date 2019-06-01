package com.seckill.dis.cache.service;

import com.seckill.dis.common.api.cache.RedisServiceApi;
import org.apache.dubbo.config.annotation.Service;

/**
 * redis服务实现
 *
 * @author noodle
 */
@Service(interfaceClass = RedisServiceApi.class)
public class RedisServiceImpl implements RedisServiceApi {
}
