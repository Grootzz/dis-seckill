package com.seckill.dis.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具
 *
 * @author noodle
 */
public class DBUtil {

    private static Properties properties;

    // 从属性文件中加载数据库配置信息
    static {
        try {
            InputStream resource = DBUtil.class.getClassLoader().getResourceAsStream("application.properties");
            properties = new Properties();
            properties.load(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接对象
     *
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection getConn() throws ClassNotFoundException, SQLException {
        String url = properties.getProperty("spring.datasource.url");
        String username = properties.getProperty("spring.datasource.username");
        String password = properties.getProperty("spring.datasource.password");
        String driver = properties.getProperty("spring.datasource.driver-class-name");
        Class.forName(driver);// 加载驱动
        return DriverManager.getConnection(url, username, password);
    }
}
