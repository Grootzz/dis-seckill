package com.seckill.dis.user.service;

import com.seckill.dis.common.api.cache.DLockApi;
import com.seckill.dis.common.api.cache.RedisServiceApi;
import com.seckill.dis.common.api.cache.vo.SkUserKeyPrefix;
import com.seckill.dis.common.api.user.UserServiceApi;
import com.seckill.dis.common.api.user.vo.LoginVo;
import com.seckill.dis.common.api.user.vo.RegisterVo;
import com.seckill.dis.common.api.user.vo.UserInfoVo;
import com.seckill.dis.common.api.user.vo.UserVo;
import com.seckill.dis.common.exception.GlobalException;
import com.seckill.dis.common.result.CodeMsg;
import com.seckill.dis.common.util.MD5Util;
import com.seckill.dis.common.util.UUIDUtil;
import com.seckill.dis.user.domain.SeckillUser;
import com.seckill.dis.user.persistence.SeckillUserMapper;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;
import java.util.Date;
import java.util.UUID;

@Service(interfaceClass = UserServiceApi.class)
public class UserServiceImpl implements UserServiceApi {

    private static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private SeckillUserMapper userMapper;

    /**
     * 由于需要将一个cookie对应的用户存入第三方缓存中，这里用redis，所以需要引入redis serice
     */
    @Reference(interfaceClass = RedisServiceApi.class)
    private RedisServiceApi redisService;

    @Reference(interfaceClass = DLockApi.class)
    private DLockApi dLock;

    @Override
    public int login(String username, String password) {
        return 45;
    }

    /**
     * 注册用户
     *
     * @param userModel 用户vo
     * @return 状态码
     */
    @Override
    public CodeMsg register(RegisterVo userModel) {

        // 加锁
        String uniqueValue = UUIDUtil.uuid() + "-" + Thread.currentThread().getId();
        String lockKey = "redis-lock" + userModel.getPhone();
        boolean lock = dLock.lock(lockKey, uniqueValue, 60 * 1000);
        if (!lock)
            return CodeMsg.WAIT_REGISTER_DONE;
        logger.debug("注册接口加锁成功");

        // 检查用户是否注册
        SeckillUser user = this.getSeckillUserByPhone(userModel.getPhone());

        // 用户已经注册
        if (user != null) {
            return CodeMsg.USER_EXIST;
        }

        // 生成skuser对象
        SeckillUser newUser = new SeckillUser();

        newUser.setPhone(userModel.getPhone());
        newUser.setNickname(userModel.getNickname());
        newUser.setHead(userModel.getHead());

        newUser.setSalt(MD5Util.SALT);

        String dbPass = MD5Util.formPassToDbPass(userModel.getPassword(), MD5Util.SALT);
        newUser.setPassword(dbPass);

        Date date = new Date(System.currentTimeMillis());
        newUser.setRegisterDate(date);

        // 写入数据库
        long id = userMapper.insertUser(newUser);

        boolean unlock = dLock.unlock(lockKey, uniqueValue);
        if (!unlock)
            return CodeMsg.REGISTER_FAIL;
        logger.debug("注册接口解锁成功");

        // 用户注册成功
        if (id > 0)
            return CodeMsg.SUCCESS;

        // 用户注册失败
        return CodeMsg.REGISTER_FAIL;
    }

    @Override
    public boolean checkUsername(String username) {
        return false;
    }

    @Override
    public UserInfoVo getUserInfo(int uuid) {
        return null;
    }

    @Override
    public UserInfoVo updateUserInfo(UserInfoVo userInfoVo) {
        return null;
    }

    /**
     * 用户登录, 要么处理成功返回true，否则会抛出全局异常
     * 抛出的异常信息会被全局异常接收，全局异常会将异常信息传递到全局异常处理器
     *
     * @param loginVo 封装了客户端请求传递过来的数据（即账号密码）
     *                （使用post方式，请求参数放在了请求体中，这个参数就是获取请求体中的数据）
     * @return 用户token
     */
    @Override
    public String login(@Valid LoginVo loginVo) {
        logger.info(loginVo.toString());

        // 获取用户提交的手机号码和密码
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        // 判断手机号是否存在(首先从缓存中取，再从数据库取)
        SeckillUser user = this.getSeckillUserByPhone(Long.parseLong(mobile));
        // 缓存中、数据库中都不存在该用户信息，直接返回
        if (user == null)
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        logger.info("用户：" + user.toString());

        // 判断手机号对应的密码是否一致
        String dbPassword = user.getPassword();
        String dbSalt = user.getSalt();
        String calcPass = MD5Util.formPassToDbPass(password, dbSalt);
        if (!calcPass.equals(dbPassword))
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);

        // 执行到这里表明登录成功，更新用户cookie
        // 生成cookie
        String token = UUIDUtil.uuid();
        // 每次访问都会生成一个新的session存储于redis和反馈给客户端，一个session对应存储一个user对象
        redisService.set(SkUserKeyPrefix.TOKEN, token, user);
        return token;
    }

    @Override
    public UserVo getUserByPhone(long phone) {
        UserVo userVo = new UserVo();
        SeckillUser user = userMapper.getUserByPhone(phone);

        userVo.setUuid(user.getUuid());
        userVo.setSalt(user.getSalt());
        userVo.setRegisterDate(user.getRegisterDate());
        userVo.setPhone(user.getPhone());
        userVo.setPassword(user.getPassword());
        userVo.setNickname(user.getNickname());
        userVo.setLoginCount(user.getLoginCount());
        userVo.setLastLoginDate(user.getLastLoginDate());
        userVo.setHead(user.getHead());

        return userVo;
    }

    /**
     * 根据 phone 查询秒杀用户信息
     * <p>
     * 对象级缓存
     * 从缓存中查询 SeckillUser 对象，如果 SeckillUser 在缓存中存在，则直接返回，否则从数据库返回
     *
     * @param phone 用户手机号码
     * @return SeckillUser
     */
    private SeckillUser getSeckillUserByPhone(long phone) {

        // 1. 从redis中获取用户数据缓存
        SeckillUser user = redisService.get(SkUserKeyPrefix.SK_USER_PHONE, "_" + phone, SeckillUser.class);

        if (user != null)
            return user;

        // 2. 如果缓存中没有用户数据，则从数据库中查询数据并将数据写入缓存
        // 先从数据库中取出数据
        user = userMapper.getUserByPhone(phone);
        // 然后将数据返回并将数据缓存在redis中
        if (user != null)
            redisService.set(SkUserKeyPrefix.SK_USER_PHONE, "_" + phone, user);
        return user;
    }
}
