package com.ljs.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * MD5
 * @author 17996
 *
 */
public class MD5Util {
	public static String md5(String src) {
		return DigestUtils.md5Hex(src);
	}
	//客户端固定的salt，跟用户的密码做一个拼装
	private static final String salt="1a2b3c4d";
	
	public static String inputPassToFormPass(String inputPass) {
		String str=""+salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
		System.out.println(md5(str));
		return md5(str); 			//char类型计算会自动转换为int类型
	}
	//二次MD5
	public static String formPassToDBPass(String formPass,String salt) {//随机的salt
		String str=""+salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
		return md5(str); 			 
	}
	//数据库md5,使用数据库随机salt
	public static String inputPassToDbPass(String input,String saltDB) {
		String formPass=inputPassToFormPass(input);
		System.out.println(formPass);
		String dbPass=formPassToDBPass(formPass,saltDB);
		return dbPass;
	}
	
	public static void main(String[] args) {
//		System.out.println(inputPassToFormPass("123456"));
//		String salt="1a2b3c4d";
//		System.out.println(formPassToDBPass(inputPassToFormPass("123456"),salt));
		System.out.println(inputPassToDbPass("111111",salt));
		
		System.out.println(formPassToDBPass("d018506bc314a32b93eb214102399a63",salt));
		//d018506bc314a32b93eb214102399a63
	}
}
