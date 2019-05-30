package com.ljs.miaosha.service;

import com.ljs.miaosha.dao.OrderDao;
import com.ljs.miaosha.domain.MiaoshaOrder;
import com.ljs.miaosha.domain.MiaoshaUser;
import com.ljs.miaosha.domain.OrderInfo;
import com.ljs.miaosha.redis.OrderKey;
import com.ljs.miaosha.redis.RedisService;
import com.ljs.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {
	@Autowired
	OrderDao orderDao;
	@Autowired
	RedisService redisService;
	
	
	
	/**
	 * 代码2.0
	 * 做一个优化，不用每次都去查数据库
	 * 生成订单的时候，将订单同时写入到缓存里面去。
	 */
	
	
	/**
	 * 判断是否秒杀到某商品，即去miaosha_order里面去查找是否有记录userId和goodsId的一条数据。
	 * 根据用户userId和goodsId判断是否有者条订单记录，有则返回此纪录
	 * 
	 * @param id
	 * @param goodsId
	 * @return
	 */
	public MiaoshaOrder getMiaoshaOrderByUserIdAndCoodsId_Cache(Long userId, Long goodsId) {
		//1.先去缓存里面取得
		MiaoshaOrder morder=redisService.get(OrderKey.getMiaoshaOrderByUidAndGid, ""+userId+"_"+goodsId, MiaoshaOrder.class);
		return morder;
		//return orderDao.getMiaoshaOrderByUserIdAndCoodsId(userId,goodsId);
	}
	
	
	/**
	 * 生成订单,事务,同时写入到缓存
	 * @param user
	 * @param goodsvo
	 * @return
	 */
	@Transactional
	public OrderInfo createOrder_Cache(MiaoshaUser user, GoodsVo goodsvo) {
		//1.生成order_info
		OrderInfo orderInfo=new OrderInfo();
		orderInfo.setDeliveryAddrId(0L);//long类型 private Long deliveryAddrId;   L
		orderInfo.setCreateDate(new Date());
		orderInfo.setGoodsCount(1);
		orderInfo.setGoodsId(goodsvo.getId());
		//秒杀价格
		orderInfo.setGoodsPrice(goodsvo.getMiaoshaPrice());
		orderInfo.setOrderChannel(1);
		//订单状态  ---0-新建未支付  1-已支付  2-已发货  3-已收货
		orderInfo.setOrderStatus(0);
		//用户id
		orderInfo.setUserId(user.getId());
		//返回orderId
		long orderId=orderDao.insert(orderInfo);
		System.out.println("-----orderId:"+orderId);
		
		OrderInfo orderquery=orderDao.selectorderInfo(user.getId(), goodsvo.getId());
		long orderIdquery=orderquery.getId();
		System.out.println("-----orderIdquery:"+orderIdquery);
		
		//2.生成miaosha_order
		MiaoshaOrder miaoshaorder =new MiaoshaOrder();
		miaoshaorder.setGoodsId(goodsvo.getId());
		//将订单id传给秒杀订单里面的订单orderid
		miaoshaorder.setOrderId(orderIdquery);
		miaoshaorder.setUserId(user.getId());
		orderDao.insertMiaoshaOrder(miaoshaorder);
		//set(KeyPrefix prefix,String key,T value)   设置缓存数据。
		redisService.set(OrderKey.getMiaoshaOrderByUidAndGid, ""+user.getId()+"_"+goodsvo.getId(), miaoshaorder);
		return orderInfo;
	}
	
	
	
	
	
	
	/**
	 * 代码1.0
	 * 根据用户userId和goodsId判断是否有者条订单记录，有则返回此纪录
	 * 
	 * @param id
	 * @param goodsId
	 * @return
	 */
	public MiaoshaOrder getMiaoshaOrderByUserIdAndCoodsId(Long userId, Long goodsId) {
		return orderDao.getMiaoshaOrderByUserIdAndCoodsId(userId,goodsId);
	}
	
	
	/**
	 * 生成订单,事务
	 * @param user
	 * @param goodsvo
	 * @return
	 */
	@Transactional
	public OrderInfo createOrder(MiaoshaUser user, GoodsVo goodsvo) {
		//1.生成order_info
		OrderInfo orderInfo=new OrderInfo();
		orderInfo.setDeliveryAddrId(0L);//long类型 private Long deliveryAddrId;   L
		orderInfo.setCreateDate(new Date());
		orderInfo.setGoodsCount(1);
		orderInfo.setGoodsId(goodsvo.getId());
		//秒杀价格
		orderInfo.setGoodsPrice(goodsvo.getMiaoshaPrice());
		orderInfo.setOrderChannel(1);
		//订单状态  ---0-新建未支付  1-已支付  2-已发货  3-已收货
		orderInfo.setOrderStatus(0);
		//用户id
		orderInfo.setUserId(user.getId());
		//返回orderId
		//long orderId=
		orderDao.insert(orderInfo);
		//2.生成miaosha_order
		MiaoshaOrder miaoshaorder =new MiaoshaOrder();
		miaoshaorder.setGoodsId(goodsvo.getId());
		//将订单id传给秒杀订单里面的订单orderid
		miaoshaorder.setOrderId(orderInfo.getId());
		miaoshaorder.setUserId(user.getId());
		orderDao.insertMiaoshaOrder(miaoshaorder);
		return orderInfo;
	}
	
	
	public OrderInfo getOrderByOrderId(long orderId) {
		return orderDao.getOrderByOrderId(orderId);
	}
	
	
}
