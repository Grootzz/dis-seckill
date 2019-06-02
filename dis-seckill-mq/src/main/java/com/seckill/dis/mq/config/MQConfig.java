package com.seckill.dis.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过配置文件获取消息队列
 *
 * @author noodle
 */
@Configuration
public class MQConfig {

    /**
     * 消息队列名
     */
    public static final String SECKILL_QUEUE = "seckill.queue";


    /**
     * Direct模式 交换机exchange
     * 生成用于秒杀的queue
     *
     * @return
     */
    @Bean
    public Queue seckillQueue() {
        return new Queue(SECKILL_QUEUE, true);
    }
}
