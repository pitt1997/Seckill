package com.ljs.miaosha.util;

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
import com.ljs.miaosha.domain.MiaoshaUser;

public class UserUtil {
	private static void createUser(int count) throws Exception{
		List<MiaoshaUser> users=new ArrayList<MiaoshaUser>();
		//生成用户
		for(int i=0;i<count;i++) {
			MiaoshaUser user=new MiaoshaUser();
			user.setId(15200000000L+i);//注意，两数相加，不会超出数位数
			user.setLoginCount(1);
			user.setNickname("user"+i);
			user.setRegisterDate(new Date());
			user.setHead("/user/useri.png");
			user.setSalt("1a2b3c4d");
			user.setPwd(MD5Util.inputPassToDbPass("123456", user.getSalt()));
			users.add(user);
		}
		System.out.println("craete users ----insert to db");
		//插入数据库
		Connection conn=DBUtil.getConn();
		String sql="insert into miaosha_user (login_count,nickname,register_date,salt,pwd,id,head) values"
				+ "(?,?,?,?,?,?,?)";
		PreparedStatement pstmt=conn.prepareStatement(sql);
		//生成用户
		for(int i=0;i<users.size();i++) {
			MiaoshaUser user=users.get(i);
			pstmt.setInt(1, user.getLoginCount());
			pstmt.setString(2, user.getNickname());
			pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
			pstmt.setString(4, user.getSalt());
			pstmt.setString(5, MD5Util.inputPassToDbPass("123456", user.getSalt()));
			pstmt.setLong(6,user.getId());
			pstmt.setString(7, user.getHead());
			//作用?
			pstmt.addBatch();
		}
		pstmt.executeBatch();
		pstmt.close();
		conn.close();
		System.out.println("登录，生成token");
		//登录，使之生成一个token
		String urlString="http://localhost:8080/login/do_login_test";
		File file=new File("D:/tokens.txt");
		if(file.exists()) {
			file.delete();
		}
		RandomAccessFile raf=new RandomAccessFile(file,"rw");
		file.createNewFile();
		raf.seek(0);
		for(int i=0;i<users.size();i++) {
			MiaoshaUser user=users.get(i);
			URL url=new URL(urlString);
			HttpURLConnection co=(HttpURLConnection) url.openConnection();
			co.setRequestMethod("POST");
			co.setDoOutput(true);
			OutputStream out=co.getOutputStream();
			String params="mobile="+user.getId()+"&password="
			+MD5Util.formPassToDBPass("123456", user.getSalt());
			out.write(params.getBytes());
			out.flush();
			InputStream inputStream=co.getInputStream();
			ByteArrayOutputStream bout=new ByteArrayOutputStream();
			byte buff[]=new byte[1024];
			int len=0;
			while((len=inputStream.read(buff))>=0) {
				bout.write(buff,0,len);
			}
			inputStream.close();
			bout.close();
			String response=new String(bout.toByteArray());
			JSONObject jo=JSON.parseObject(response);
			String token=jo.getString("data");
			System.out.println("user:"+user.getId()+"	token:"+token);
			String row=user.getId()+","+token;
			raf.seek(raf.length());
			raf.write(row.getBytes());
			raf.write("\r\n".getBytes());
			System.out.println("write to file : "+user.getId());
		}
	}
	public static void main(String[] args) throws Exception {
		//createUser(5000);
		createUser(1000);
	}
}
