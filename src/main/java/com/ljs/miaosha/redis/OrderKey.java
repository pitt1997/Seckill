package com.ljs.miaosha.redis;
/**
 * 暂时不设置过期时间
 * @author 17996
 *
 */
public class OrderKey extends BasePrefix{

	public OrderKey(String prefix) {
		super(prefix);
	}
	public static OrderKey getMiaoshaOrderByUidAndGid=new OrderKey("ms_uidgid");

}
