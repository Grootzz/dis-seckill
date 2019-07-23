package com.seckill.dis.user.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seckill.dis.common.util.DBUtil;
import com.seckill.dis.common.util.MD5Util;
import com.seckill.dis.user.domain.SeckillUser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 生成用户数据，用于压测
 *
 * @author noodle
 */
public class UserUtil {
    static String PASSWORD = "000000";

    public static void createUser(int count) throws IOException {

        List<SeckillUser> users = new ArrayList<>(count);

        // 生成用户信息
        generateMiaoshaUserList(count, users);

        // 将用户信息插入数据库，以便在后面模拟用户登录时可以找到该用户，从而可以生成token返会给客户端，然后保存到文件中用于压测
        // 首次生成数据库信息的时候需要调用这个方法，非首次需要注释掉
        // insertSeckillUserToDB(users);
        // 模拟用户登录，生成token
        System.out.println("start to login...");
        String urlString = "http://localhost:8080/login/create_token";
        File file = new File("E:\\JavaProject\\Web\\seckill\\tokens.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
        file.createNewFile();
        accessFile.seek(0);

        for (int i = 0; i < users.size(); i++) {
            // 模拟用户登录
            SeckillUser user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            OutputStream out = httpURLConnection.getOutputStream();
            String params = "mobile=" + user.getPhone() + "&password=" + MD5Util.inputPassToFormPass(PASSWORD);
            out.write(params.getBytes());
            out.flush();

            // 生成token
            InputStream inputStream = httpURLConnection.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            JSONObject jo = JSON.parseObject(response);
            String token = jo.getString("data");// data为edu.uestc.controller.result.Result中的字段
            System.out.println("create token : " + user.getPhone());
            // 将token写入文件中，用于压测时模拟客户端登录时发送的token
            String row = user.getPhone() + "," + token;
            accessFile.seek(accessFile.length());
            accessFile.write(row.getBytes());
            accessFile.write("\r\n".getBytes());
            System.out.println("write to file : " + user.getPhone());
        }
        accessFile.close();
        System.out.println("write token to file done!");
    }

    /**
     * 生成用户信息
     *
     * @param count 生成的用户数量
     * @param users 用于存储用户的list
     */
    private static void generateMiaoshaUserList(int count, List<SeckillUser> users) {
        for (int i = 0; i < count; i++) {
            SeckillUser user = new SeckillUser();
            user.setPhone(19800000000L + i);// 模拟11位的手机号码
            user.setLoginCount(1);
            user.setNickname("user-" + i);
            user.setRegisterDate(new Date());
            user.setSalt("1a2b3c4d");
            user.setPassword(MD5Util.inputPassToDbPass(PASSWORD, user.getSalt()));
            users.add(user);
        }
    }

    /**
     * 将用户信息插入数据库中
     *
     * @param users 待插入的用户信息
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private static void insertSeckillUserToDB(List<SeckillUser> users) throws ClassNotFoundException, SQLException {
        System.out.println("start create user...");
        Connection conn = DBUtil.getConn();
        String sql = "INSERT INTO miaosha_user(login_count, nickname, register_date, salt, password, id)VALUES(?,?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < users.size(); i++) {
            SeckillUser user = users.get(i);
            pstmt.setInt(1, user.getLoginCount());
            pstmt.setString(2, user.getNickname());
            pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
            pstmt.setString(4, user.getSalt());
            pstmt.setString(5, user.getPassword());
            pstmt.setLong(6, user.getPhone());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.close();
        conn.close();
        System.out.println("insert to db done!");
    }

//    public static void main(String[] args) throws IOException {
//        createUser(5000);
//    }
}
