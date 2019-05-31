package com.seckill.dis.goods.redis;


/**
 * redis中，用于商品信息的key
 *
 * @author noodle
 */
public class GoodsKeyPrefix extends BaseKeyPrefix {
    public GoodsKeyPrefix(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    // 缓存在redis中的商品列表页面的key的前缀
    public static GoodsKeyPrefix goodsListKeyPrefix = new GoodsKeyPrefix(60, "goodsList");

    // 缓存在redis中的商品详情页面的key的前缀
    public static GoodsKeyPrefix goodsDetailKeyPrefix = new GoodsKeyPrefix(60, "goodsDetail");

    // 缓存在redis中的商品库存的前缀(缓存过期时间为永久)
    public static GoodsKeyPrefix seckillGoodsStockPrefix = new GoodsKeyPrefix(0, "goodsStock");
}
