package com.lijs.seckill.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lijs.seckill.domain.SeckillUser;

public class UserUtil {
    private static void createUser(int count) throws Exception {
        List<SeckillUser> users = new ArrayList<SeckillUser>();
        for (int i = 0; i < count; i++) {
            SeckillUser user = new SeckillUser();
            user.setId(15200000000L + i);
            user.setLoginCount(1);
            user.setNickname("user" + i);
            user.setRegisterDate(new Date());
            user.setHead("/user/useri.png");
            user.setSalt("1a2b3c4d");
            user.setPwd(MD5Util.inputPassToDbPass("123456", user.getSalt()));
            users.add(user);
        }
        System.out.println("create users...");
        Connection conn = DBUtil.getConn();
        String sql = "insert into seckill_user (login_count,nickname,register_date,salt,pwd,id,head) values"
                + "(?,?,?,?,?,?,?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (SeckillUser user : users) {
            stmt.setInt(1, user.getLoginCount());
            stmt.setString(2, user.getNickname());
            stmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
            stmt.setString(4, user.getSalt());
            stmt.setString(5, MD5Util.inputPassToDbPass("123456", user.getSalt()));
            stmt.setLong(6, user.getId());
            stmt.setString(7, user.getHead());
            stmt.addBatch();
        }
        stmt.executeBatch();
        stmt.close();
        conn.close();
        System.out.println("登录，生成token");
        // 登录，使之生成一个token
        String urlString = "http://localhost:8080/login/token_test";
        // 记录Token
        File file = new File("D:/tokens.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for (int i = 0; i < users.size(); i++) {
            SeckillUser user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();
            String params = "mobile=" + user.getId() + "&password="
                    + MD5Util.formPassToDBPass("123456", user.getSalt());
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = co.getInputStream();
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
            String token = jo.getString("data");
            System.out.println("user:" + user.getId() + "	token:" + token);
            String row = user.getId() + "," + token;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file : " + user.getId());
        }
    }

    public static void main(String[] args) throws Exception {
        createUser(1000);
    }
}
