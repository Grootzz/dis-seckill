package com.seckill.dis.gateway.seckill;

import com.seckill.dis.common.api.goods.GoodsServiceApi;
import com.seckill.dis.common.api.goods.vo.GoodsVo;
import com.seckill.dis.common.api.order.OrderServiceApi;
import com.seckill.dis.common.api.seckill.SeckillServiceApi;
import com.seckill.dis.common.api.seckill.vo.VerifyCodeVo;
import com.seckill.dis.common.api.user.vo.UserVo;
import com.seckill.dis.common.domain.SeckillOrder;
import com.seckill.dis.common.result.CodeMsg;
import com.seckill.dis.common.result.Result;
import com.seckill.dis.common.util.MD5Util;
import com.seckill.dis.common.util.UUIDUtil;
import com.seckill.dis.common.util.VerifyCodeUtil;
import com.seckill.dis.gateway.config.access.AccessLimit;
import com.seckill.dis.gateway.rabbitmq.MQSender;
import com.seckill.dis.gateway.rabbitmq.SeckillMessage;
import com.seckill.dis.gateway.redis.GoodsKeyPrefix;
import com.seckill.dis.gateway.redis.OrderKeyPrefix;
import com.seckill.dis.gateway.redis.RedisService;
import com.seckill.dis.gateway.redis.SeckillKeyPrefix;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 秒杀接口
 *
 * @author noodle
 */
@Controller
@RequestMapping("/seckill/")
public class SeckillController implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(SeckillController.class);

    @Autowired
    RedisService redisService;

    @Reference(interfaceClass = GoodsServiceApi.class)
    GoodsServiceApi goodsService;

    @Reference(interfaceClass = SeckillServiceApi.class)
    SeckillServiceApi seckillService;

    @Reference(interfaceClass = OrderServiceApi.class)
    OrderServiceApi orderService;

    @Autowired
    MQSender sender;

    /**
     * 用于内存标记，标记库存是否为空，从而减少对redis的访问
     */
    private Map<Long, Boolean> localOverMap = new HashMap<>();

    /**
     * 获取秒杀接口地址
     * 每一次点击秒杀，都会生成一个随机的秒杀地址返回给客户端
     * 对秒杀的次数做限制（通过自定义拦截器注解完成）
     *
     * @param model
     * @param user
     * @param goodsId    秒杀的商品id
     * @param verifyCode 验证码
     * @return 被隐藏的秒杀接口路径
     */

    @AccessLimit(seconds = 5, maxAccessCount = 5, needLogin = true)
    @RequestMapping(value = "path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillPath(Model model, UserVo user,
                                         @RequestParam("goodsId") long goodsId,
                                         @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode) {

        // 在执行下面的逻辑之前，会相对path请求进行拦截处理（@AccessLimit， AccessInterceptor），防止访问次数过于频繁，对服务器造成过大的压力
        model.addAttribute("user", user);

        if (user == null || goodsId <= 0) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        // 校验验证码
        boolean check = this.checkVerifyCode(user, goodsId, verifyCode);

        if (!check)
            return Result.error(CodeMsg.REQUEST_ILLEGAL);// 检验不通过，请求非法

        // 检验通过，获取秒杀路径
        String path = this.createSkPath(user, goodsId);
        // 向客户端回传随机生成的秒杀地址
        return Result.success(path);
    }

    /**
     * c5: 秒杀逻辑（页面静态化分离，不需要直接将页面返回给客户端，而是返回客户端需要的页面动态数据，返回数据时json格式）
     * <p>
     * QPS:1306
     * 5000 * 10
     * <p>
     * GET/POST的@RequestMapping是有区别的
     * <p>
     * c6： 通过随机的path，客户端隐藏秒杀接口
     * <p>
     * 优化: 不同于每次都去数据库中读取秒杀订单信息，而是在第一次生成秒杀订单成功后，
     * 将订单存储在redis中，再次读取订单信息的时候就直接从redis中读取
     *
     * @param model
     * @param user
     * @param goodsId
     * @param path    隐藏的秒杀地址，为客户端回传的path，最初也是有服务端产生的
     * @return 订单详情或错误码
     */
    // {path}为客户端回传的path，最初也是有服务端产生的
    @RequestMapping(value = "{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> doSeckill(Model model, UserVo user,
                                     @RequestParam("goodsId") long goodsId,
                                     @PathVariable("path") String path) {

        model.addAttribute("user", user);
        // 1. 如果用户为空，则返回登录界面
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);

        // 验证path是否正确
        boolean check = this.checkPath(user, goodsId, path);

        if (!check)
            return Result.error(CodeMsg.REQUEST_ILLEGAL);// 请求非法

        // 通过内存标记，减少对redis的访问，秒杀未结束才继续访问redis
        Boolean over = localOverMap.get(goodsId);
        if (over)
            return Result.error(CodeMsg.SECKILL_OVER);

        // 预减库存
        Long stock = redisService.decr(GoodsKeyPrefix.seckillGoodsStockPrefix, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);// 秒杀结束。标记该商品已经秒杀结束
            return Result.error(CodeMsg.SECKILL_OVER);
        }

        // 判断是否重复秒杀
        // 从redis中取缓存，减少数据库的访问
        SeckillOrder order = redisService.get(OrderKeyPrefix.getSeckillOrderByUidGid, ":" + user.getUuid() + "_" + goodsId, SeckillOrder.class);
        // 如果缓存中不存该数据，则从数据库中取
        if (order == null) {
            order = orderService.getSeckillOrderByUserIdAndGoodsId(user.getUuid(), goodsId);
        }

        if (order != null) {
            return Result.error(CodeMsg.REPEATE_SECKILL);
        }

        // 商品有库存且用户为秒杀商品，则将秒杀请求放入MQ
        SeckillMessage message = new SeckillMessage();
        message.setUser(user);
        message.setGoodsId(goodsId);

        // 放入MQ
        sender.sendSkMessage(message);
        return Result.success(0); // 排队中
    }

    /**
     * 用于返回用户秒杀的结果
     *
     * @param model
     * @param user
     * @param goodsId
     * @return orderId：成功, -1：秒杀失败, 0： 排队中
     */
    @RequestMapping(value = "result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> getSeckillResult(Model model, UserVo user,
                                         @RequestParam("goodsId") long goodsId) {

        model.addAttribute("user", user);

        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        long result = seckillService.getSeckillResult(user.getUuid(), goodsId);
        return Result.success(result);
    }

    /**
     * goods_detail.htm: $("#verifyCodeImg").attr("src", "/seckill/verifyCode?goodsId=" + $("#goodsId").val());
     * 使用HttpServletResponse的输出流返回客户端异步获取的验证码（异步获取的代码如上所示）
     *
     * @param response
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getVerifyCode(HttpServletResponse response, UserVo user,
                                        @RequestParam("goodsId") long goodsId) {
        logger.info("获取验证码");
        if (user == null || goodsId <= 0)
            return Result.error(CodeMsg.SESSION_ERROR);

        // 创建验证码
        try {
            // String verifyCodeJsonString = seckillService.createVerifyCode(user, goodsId);
            VerifyCodeVo verifyCode = VerifyCodeUtil.createVerifyCode();

            // 验证码结果预先存到redis中
            redisService.set(SeckillKeyPrefix.seckillVerifyCode, user.getUuid() + "," + goodsId, verifyCode.getExpResult());
            ServletOutputStream out = response.getOutputStream();
            // 将图片写入到resp对象中
            ImageIO.write(verifyCode.getImage(), "JPEG", out);
            out.close();
            out.flush();

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SECKILL_FAIL);
        }
    }

    /**
     * 检验检验码的计算结果
     *
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    private boolean checkVerifyCode(UserVo user, long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0) {
            return false;
        }

        // 从redis中获取验证码计算结果
        Integer oldCode = redisService.get(SeckillKeyPrefix.seckillVerifyCode, user.getUuid() + "," + goodsId, Integer.class);
        if (oldCode == null || oldCode - verifyCode != 0) {// !!!!!!
            return false;
        }

        // 如果校验不成功，则说明校验码过期
        redisService.delete(SeckillKeyPrefix.seckillVerifyCode, user.getUuid() + "," + goodsId);
        return true;
    }

    /**
     * 创建秒杀地址, 并将其存储在redis中
     *
     * @param user
     * @param goodsId
     * @return
     */
    public String createSkPath(UserVo user, long goodsId) {

        if (user == null || goodsId <= 0) {
            return null;
        }

        // 随机生成秒杀地址
        String path = MD5Util.md5(UUIDUtil.uuid() + "123456");
//        String path = "a";
        // 将随机生成的秒杀地址存储在redis中（保证不同的用户和不同商品的秒杀地址是不一样的）
        redisService.set(SeckillKeyPrefix.seckillPath, "" + user.getUuid() + "_" + goodsId, path);
        return path;
    }

    /**
     * 验证路径是否正确
     *
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    public boolean checkPath(UserVo user, long goodsId, String path) {
        if (user == null || path == null)
            return false;
        // 从redis中读取出秒杀的path变量是否为本次秒杀操作执行前写入redis中的path
        String oldPath = redisService.get(SeckillKeyPrefix.seckillPath, "" + user.getUuid() + "_" + goodsId, String.class);
        return path.equals(oldPath);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        List<GoodsVo> goods = goodsService.listGoodsVo();
        if (goods == null) {
            return;
        }

        // 将商品的库存信息存储在redis中
        for (GoodsVo good : goods) {
            redisService.set(GoodsKeyPrefix.seckillGoodsStockPrefix, "" + good.getId(), good.getStockCount());
            localOverMap.put(good.getId(), false); // 在系统启动时，标记库存不为空
        }
    }
}
