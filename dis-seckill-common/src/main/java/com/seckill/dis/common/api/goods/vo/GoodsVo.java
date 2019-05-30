package com.seckill.dis.common.api.goods.vo;

import com.seckill.dis.common.domain.Goods;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品信息（并且包含商品的秒杀信息）
 * 商品信息和商品的秒杀信息是存储在两个表中的（goods和seckill_goods）
 * 继承Goods便具有了goods表的信息，再额外添加上seckill_goods的信息即可
 *
 * @author noodle
 */
public class GoodsVo extends Goods implements Serializable {

    /*只包含了部分seckill_goods表的信息*/
    private Double seckillPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;

    public Double getSeckillPrice() {
        return seckillPrice;
    }

    public void setSeckillPrice(Double seckillPrice) {
        this.seckillPrice = seckillPrice;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
