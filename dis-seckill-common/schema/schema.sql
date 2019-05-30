

CREATE DATABASE seckill;

USE seckill;
# ---------------------------------------------------------------------
# 用户表
CREATE TABLE user (
  id   INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(20) NOT NULL
)
  ENGINE = INNODB;
# 向用户表中插入一条数据
INSERT INTO user VALUES (NULL, 'ffl');
INSERT INTO user VALUES (NULL, 'ff2');
# ---------------------------------------------------------------------
# 秒杀用户表
CREATE TABLE seckill_user (
  id              BIGINT(20)   NOT NULL
  COMMENT '用户id，手机号码',
  nickname        VARCHAR(255) NOT NULL
  COMMENT '昵称',
  password        VARCHAR(32)  DEFAULT NULL
  COMMENT 'MD5(MD5(password明文+固定salt) + salt)',
  salt            VARCHAR(10)  DEFAULT NULL,
  head            VARCHAR(128) DEFAULT NULL
  COMMENT '头像，云存储的id',
  register_date   DATETIME     DEFAULT NULL
  COMMENT '注册时间',
  last_login_date DATETIME     DEFAULT NULL
  COMMENT '上次登录时间',
  login_count     INT(11)      DEFAULT 0
  COMMENT '登录次数',
  PRIMARY KEY (id)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
# 插入一条记录（未经过MD5的密码为000000, 两次MD5后的密码为记录中的密码，两次MD5的salt一样）
INSERT INTO miaosha_user (id, nickname, password, salt)
VALUES (18342390420, 'Noodle', '5e7b3a9754c2777f96174d4ccb980d23', '1a2b3c4d');
