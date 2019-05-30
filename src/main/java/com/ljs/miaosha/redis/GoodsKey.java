package com.ljs.miaosha.redis;

public class GoodsKey extends BasePrefix{
	//考虑页面缓存有效期比较短
	public GoodsKey(int expireSeconds,String prefix) {
		super(expireSeconds,prefix);
	}
	//goods_list页面          1分钟
	public static GoodsKey getGoodsList=new GoodsKey(60,"gl");
	//goods_detail页面       1分钟
	public static GoodsKey getGoodsDetail=new GoodsKey(60,"gd");
	//秒杀的商品的数量stock，0不失效
	public static GoodsKey getMiaoshaGoodsStock=new GoodsKey(0,"gs");
}
