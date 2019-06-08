package com.seckill.dis.user;

import com.seckill.dis.user.persistence.SeckillUserMapper;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@EnableDubbo
@SpringBootApplication
public class UserApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(UserApplication.class, args);
        //SeckillUserMapper bean = context.getBean(SeckillUserMapper.class);
        //System.out.println(bean.getUserByPhone(14785782354L));
    }
}
