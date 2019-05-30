package com.seckill.dis.user.persistence;

import com.seckill.dis.user.domain.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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
}
