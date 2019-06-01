package com.seckill.dis.order.service;

import com.seckill.dis.common.api.cache.RedisServiceApi;
import com.seckill.dis.common.api.cache.vo.OrderKeyPrefix;
import com.seckill.dis.common.api.goods.vo.GoodsVo;
import com.seckill.dis.common.api.order.OrderServiceApi;
import com.seckill.dis.common.api.user.vo.UserVo;
import com.seckill.dis.common.domain.OrderInfo;
import com.seckill.dis.common.domain.SeckillOrder;
import com.seckill.dis.order.persistence.OrderMapper;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 订单服务实现
 *
 * @author noodle
 */
@Service(interfaceClass = OrderServiceApi.class)
public class OrderServiceImpl implements OrderServiceApi {

    private static Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    OrderMapper orderMapper;

    @Reference(interfaceClass = RedisServiceApi.class)
    RedisServiceApi redisService;

    @Override
    public OrderInfo getOrderById(long orderId) {
        return orderMapper.getOrderById(orderId);
    }

    @Override
    public SeckillOrder getSeckillOrderByUserIdAndGoodsId(long userId, long goodsId) {
        return orderMapper.getSeckillOrderByUserIdAndGoodsId(userId, goodsId);
    }

    /**
     * 创建订单
     * <p>
     * 首先向数据库中写入数据，然后将数据写到缓存中，这样可以保证缓存和数据库中的数据的一致
     * 1. 向 order_info 中插入订单详细信息
     * 2. 向 seckill_order 中插入订单概要
     * 两个操作需要构成一个数据库事务
     *
     * @param user
     * @param goods
     * @return
     */
    @Transactional
    @Override
    public OrderInfo createOrder(UserVo user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        SeckillOrder seckillOrder = new SeckillOrder();

        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);// 订单中商品的数量
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSeckillPrice());// 秒杀价格
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getUuid());

        // 将订单信息插入 order_info 表中
        long orderId = orderMapper.insert(orderInfo);
        logger.debug("将订单信息插入 order_info 表中: 记录为" + orderId);

        seckillOrder.setGoodsId(goods.getId());
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setUserId(user.getUuid());

        // 将秒杀订单插入 seckill_order 表中
        orderMapper.insertSeckillOrder(seckillOrder);
        logger.debug("将秒杀订单插入 seckill_order 表中");

        // 将秒杀订单概要信息存储于redis中
//        redisService.set(OrderKeyPrefix.getSeckillOrderByUidGid, ":" + user.getUuid() + "_" + goods.getId(), seckillOrder);
        redisService.set(OrderKeyPrefix.SK_ORDER, ":" + user.getUuid() + "_" + goods.getId(), seckillOrder);

        return orderInfo;
    }
}
