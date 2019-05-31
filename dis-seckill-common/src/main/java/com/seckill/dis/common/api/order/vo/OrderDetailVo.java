package com.seckill.dis.common.api.order.vo;

import com.seckill.dis.common.api.goods.vo.GoodsVo;
import com.seckill.dis.common.domain.OrderInfo;

/**
 * 订单详情，包含订单信息和商品信息
 * <p>
 * 用于将数据传递给客户端
 *
 * @author noodle
 */
public class OrderDetailVo {

    /**
     * 商品信息
     */
    private GoodsVo goods;
    /**
     * 订单信息
     */
    private OrderInfo order;

    public GoodsVo getGoods() {
        return goods;
    }

    public void setGoods(GoodsVo goods) {
        this.goods = goods;
    }

    public OrderInfo getOrder() {
        return order;
    }

    public void setOrder(OrderInfo order) {
        this.order = order;
    }
}
