package com.seckill.dis.common.api.seckill;

import com.seckill.dis.common.api.goods.vo.GoodsVo;
import com.seckill.dis.common.api.user.vo.UserVo;
import com.seckill.dis.common.domain.OrderInfo;

/**
 * 秒杀服务接口
 *
 * @author noodle
 */
public interface SeckillServiceApi {
    /**
     * 创建验证码
     *
     * @param user
     * @param goodsId
     * @return
     */
    String createVerifyCode(UserVo user, long goodsId);

    /**
     * 执行秒杀操作，包含以下两步：
     * 1. 从goods表中减库存
     * 2. 将生成的订单写入miaosha_order表中
     *
     * @param user  秒杀商品的用户
     * @param goods 所秒杀的商品
     * @return 生成的订单信息
     */
    OrderInfo seckill(UserVo user, GoodsVo goods);

    /**
     * 获取秒杀结果
     *
     * @param userId
     * @param goodsId
     * @return
     */
    public long getSeckillResult(Long userId, long goodsId);
}
