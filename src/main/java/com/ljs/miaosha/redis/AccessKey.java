package com.ljs.miaosha.redis;

public class AccessKey extends BasePrefix{
	//考虑页面缓存有效期比较短
	public AccessKey(int expireSeconds,String prefix) {
		super(expireSeconds,prefix);
	}
	//限制5s之内访问5次
	public static AccessKey access=new AccessKey(5,"access");
	//动态设置有效期
	public static AccessKey expire(int expireSeconds) {
		return new AccessKey(expireSeconds,"access");
	}
}
