package com.seckill.dis.user.persistence;

import com.seckill.dis.common.domain.OrderInfo;
import com.seckill.dis.user.domain.SeckillUser;
import org.apache.ibatis.annotations.*;

/**
 * seckill_user表交互接口
 *
 * @author noodle
 */
@Mapper
public interface SeckillUserMapper {
     /**
     * 通过 phone 查询用户信息
     *
     * @param phone
     * @return
     */
    SeckillUser getUserByPhone(@Param("phone") Long phone);

    /**
     * 更新用户信息
     *
     * @param updatedUser
     */
    @Update("UPDATE seckill_user SET password=#{password} WHERE id=#{id}")
    void updatePassword(SeckillUser updatedUser);


    @Insert("INSERT INTO seckill_user (phone, nickname, password, salt, head, register_date, last_login_date, login_count)"
            + "VALUES (#{phone}, #{nickname}, #{password}, #{salt}, #{head}, #{registerDate}, #{lastLoginDate}, #{loginCount})")
    // 查询出插入订单信息的表id，并返回
    @SelectKey(keyColumn = "UUID", keyProperty = "uuid", resultType = long.class, before = false, statement = "SELECT last_insert_id()")
    long insertUser(SeckillUser seckillUser);
}
