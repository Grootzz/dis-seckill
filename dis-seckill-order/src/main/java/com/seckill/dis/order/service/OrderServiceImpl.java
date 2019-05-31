package com.seckill.dis.order.service;

import com.seckill.dis.common.api.goods.vo.GoodsVo;
import com.seckill.dis.common.api.order.OrderServiceApi;
import com.seckill.dis.common.api.user.vo.UserVo;
import com.seckill.dis.common.domain.OrderInfo;
import com.seckill.dis.common.domain.SeckillOrder;
import com.seckill.dis.order.persistence.OrderMapper;
import com.seckill.dis.order.redis.OrderKeyPrefix;
import com.seckill.dis.order.redis.RedisService;
import org.apache.dubbo.config.annotation.Service;
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

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    RedisService redisService;

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
     * c5: 增加redis缓存
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

        // 将订单信息插入order_info表中
        long orderId = orderMapper.insert(orderInfo);

        seckillOrder.setGoodsId(goods.getId());
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setUserId(user.getUuid());
        // 将秒杀订单插入miaosha_order表中
        orderMapper.insertSeckillOrder(seckillOrder);

        // 将秒杀订单信息存储于redis中
        redisService.set(OrderKeyPrefix.getSeckillOrderByUidGid, ":" + user.getUuid() + "_" + goods.getId(), seckillOrder);

        return orderInfo;
    }
}
