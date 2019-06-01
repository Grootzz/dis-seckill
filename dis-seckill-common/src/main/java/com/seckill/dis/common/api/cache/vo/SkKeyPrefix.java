package com.seckill.dis.common.api.cache.vo;


/**
 * 判断秒杀状态的key前缀
 */
public class SkKeyPrefix extends BaseKeyPrefix {
    public SkKeyPrefix(String prefix) {
        super(prefix);
    }

    public SkKeyPrefix(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SkKeyPrefix isGoodsOver = new SkKeyPrefix("isGoodsOver");
    /**
     * 库存为0的商品的前缀
     */
    public static SkKeyPrefix GOODS_SK_OVER = new SkKeyPrefix("goodsSkOver");

    /**
     * 秒杀接口随机地址
     */
    public static SkKeyPrefix skPath = new SkKeyPrefix(60, "skPath");
    public static SkKeyPrefix SK_PATH = new SkKeyPrefix(60, "skPath");
    // 验证码5分钟有效
    public static SkKeyPrefix skVerifyCode = new SkKeyPrefix(300, "skVerifyCode");
    /**
     * 验证码5分钟有效
     */
    public static SkKeyPrefix VERIFY_RESULT = new SkKeyPrefix(300, "verifyResult");
}
