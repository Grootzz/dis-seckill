package com.seckill.dis.common.api.goods.vo;

import com.seckill.dis.common.api.user.vo.UserVo;

import java.io.Serializable;

/**
 * 商品详情
 * 用于将数据传递给客户端
 *
 * @author noodle
 */
public class GoodsDetailVo implements Serializable {


    private int seckillStatus = 0;
    private int remainSeconds = 0;
    private GoodsVo goods;
    private UserVo user;

    @Override
    public String toString() {
        return "GoodsDetailVo{" +
                "seckillStatus=" + seckillStatus +
                ", remainSeconds=" + remainSeconds +
                ", goods=" + goods +
                ", user=" + user +
                '}';
    }

    public int getSeckillStatus() {
        return seckillStatus;
    }

    public void setSeckillStatus(int seckillStatus) {
        this.seckillStatus = seckillStatus;
    }

    public int getRemainSeconds() {
        return remainSeconds;
    }

    public void setRemainSeconds(int remainSeconds) {
        this.remainSeconds = remainSeconds;
    }

    public GoodsVo getGoods() {
        return goods;
    }

    public void setGoods(GoodsVo goods) {
        this.goods = goods;
    }

    public UserVo getUser() {
        return user;
    }

    public void setUser(UserVo user) {
        this.user = user;
    }
}
