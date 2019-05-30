package com.ljs.miaosha.redis;
/**
 *做缓存的前缀接口 
 */
public interface KeyPrefix {
	//有效期
	public int expireSeconds();
	//前缀
	public String getPrefix();
}
