# Redis中存储的数据

```properties
# 1. redis中缓存通过用户手机号码获取的用户信息
key: SkUserKeyPrefix:id_{phone}
value: {SeckillUser}
expire: 0

# 2. redis中通过缓存的token获取用户信息
key: SkUserKeyPrefix:token_{token}
value: {SeckillUser}
expire: 30min

# 3. redis中存储的商品列表页面
key: GoodsKeyPrefix:goodsListHtml
value: {html}
expire: 1min

# 4. redis中存储验证码结果
key: SkKeyPrefix:verifyResult_{uuid}_{goodsId}
value: {verifyResult}
expire: 5min

# 5. redis中存储随机秒杀地址
key: SkKeyPrefix:skPath_{uuid}_{goodsId}
value: {path}
expire: 1min

# 6. redis中存储用户一段时间内的访问次数
key: AccessKeyPrefix:access_{URI}_{phone}
value: {count}
expire: {@AccessLimit#seconds}

# 7. redis中存储的随机秒杀地址
key: SkKeyPrefix:skPath_{userId}_{goodsId}
value: {path}
expire: 1 min

# 8. redis中存储的在系统加载时从db读取的商品库存数量
key: GoodsKeyPrefix:goodsStock_{goodsId}
value: {stock}
expire: 0

# 9. redis中存储的订单信息
key: OrderKeyPrefix:SK_ORDER:{userId}_{goodsId}
value: {SeckillOrder}
expire: 0
```
