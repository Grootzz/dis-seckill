package com.seckill.dis.mq.service;

import com.seckill.dis.common.api.mq.MqProviderApi;
import com.seckill.dis.common.api.mq.vo.SkMessage;
import com.seckill.dis.common.util.JsonUtil;
import com.seckill.dis.mq.config.MQConfig;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 消息队列服务化（消息生产者）
 *
 * @author noodle
 */

@Service(interfaceClass = MqProviderApi.class)
public class MqProviderImpl implements MqProviderApi, RabbitTemplate.ConfirmCallback {

    private static Logger logger = LoggerFactory.getLogger(MqProviderImpl.class);

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void sendSkMessage(SkMessage message) {
        String msg = JsonUtil.beanToString(message);
        logger.info("MQ send message: " + msg);
        // 第一个参数为消息队列名，第二个参数为发送的消息
        rabbitTemplate.convertAndSend(MQConfig.SECKILL_QUEUE, msg);
    }

    /**
     * MQ ack 机制
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

    }
}
