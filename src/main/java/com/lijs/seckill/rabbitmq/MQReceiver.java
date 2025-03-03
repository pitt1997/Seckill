package com.lijs.seckill.rabbitmq;

import com.lijs.seckill.service.GoodsService;
import com.lijs.seckill.service.SeckillService;
import com.lijs.seckill.service.SeckillUserService;
import com.lijs.seckill.service.OrderService;
import com.lijs.seckill.vo.GoodsVo;
import com.lijs.seckill.domain.SeckillOrder;
import com.lijs.seckill.domain.SeckillUser;
import com.lijs.seckill.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//接收者
@Service
public class MQReceiver {
	@Autowired
    GoodsService goodsService;
	@Autowired
	RedisService redisService;
	@Autowired
    SeckillUserService seckillUserService;
	//作为秒杀功能事务的Service
	@Autowired
	SeckillService seckillService;
	@Autowired
    OrderService orderService;
	
	private static Logger log=LoggerFactory.getLogger(MQReceiver.class);
	
	
	@RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)//指明监听的是哪一个queue
	public void receiveMiaosha(String message) {
		log.info("receiveMiaosha message:"+message);
		//通过string类型的message还原成bean
		//拿到了秒杀信息之后。开始业务逻辑秒杀，
		SeckillMessage mm=RedisService.stringToBean(message, SeckillMessage.class);
		SeckillUser user=mm.getUser();
		long goodsId=mm.getGoodsId();
		GoodsVo goodsvo=goodsService.getGoodsVoByGoodsId(goodsId);
		int  stockcount=goodsvo.getStockCount();		
		//1.判断库存不足
		if(stockcount<=0) {//失败			库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
			//model.addAttribute("errorMessage", CodeMsg.MIAOSHA_OVER_ERROR);
			return;
		}
		//2.判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
		SeckillOrder order = orderService.getMiaoshaOrderByUserIdAndCoodsId(user.getId(), goodsId);
		if (order != null) {// 重复下单
			// model.addAttribute("errorMessage", CodeMsg.REPEATE_MIAOSHA);
			return;
		}
		//原子操作：1.库存减1，2.下订单，3.写入秒杀订单--->是一个事务
		//seckillService.miaosha(user,goodsvo);
		seckillService.miaosha1(user,goodsvo);
		
	}
	
	
	
	
	
//	@RabbitListener(queues=MQConfig.QUEUE)//指明监听的是哪一个queue
//	public void receive(String message) {
//		log.info("receive message:"+message);
//	}
//	
//	@RabbitListener(queues=MQConfig.TOPIC_QUEUE1)//指明监听的是哪一个queue
//	public void receiveTopic1(String message) {
//		log.info("receiveTopic1 message:"+message);
//	}
//	
//	@RabbitListener(queues=MQConfig.TOPIC_QUEUE2)//指明监听的是哪一个queue
//	public void receiveTopic2(String message) {
//		log.info("receiveTopic2 message:"+message);
//	}
//	
//	@RabbitListener(queues=MQConfig.HEADER_QUEUE)//指明监听的是哪一个queue
//	public void receiveHeaderQueue(byte[] message) {
//		log.info("receive Header Queue message:"+new String(message));
//	}
}
