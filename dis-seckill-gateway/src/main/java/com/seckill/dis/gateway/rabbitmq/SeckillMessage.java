package com.seckill.dis.gateway.rabbitmq;


import com.seckill.dis.common.api.user.vo.UserVo;

/**
 * 在MQ中传递的秒杀信息
 * 包含参与秒杀的用户和商品的id
 *
 * @author noodle
 */
public class SeckillMessage {

    private UserVo user;

    private long goodsId;


    public UserVo getUser() {
        return user;
    }

    public void setUser(UserVo user) {
        this.user = user;
    }

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }

    @Override
    public String toString() {
        return "SeckillMessage{" +
                "user=" + user +
                ", goodsId=" + goodsId +
                '}';
    }
}
