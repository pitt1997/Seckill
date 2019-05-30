package com.ljs.miaosha.rabbitmq;

import com.ljs.miaosha.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


//发送者
@Service
public class MQSender {
	
	private static Logger log=LoggerFactory.getLogger(MQSender.class);
	@Autowired
	RedisService redisService;
	@Autowired
	AmqpTemplate amqpTemplate;
	/**
	 * 发送秒杀信息，使用derict模式的交换机。（包含秒杀用户信息，秒杀商品id）
	 */
	public void sendMiaoshaMessage(MiaoshaMessage mmessage) {
		// 将对象转换为字符串
		String msg = RedisService.beanToString(mmessage);
		log.info("send message:" + msg);
		// 第一个参数队列的名字，第二个参数发出的信息
		amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg);
	}
	
	
	
	
	/**
	 * 下面是测试。
	 * @param message
	 */
//	public void send(Object message) {
//		//将对象转换为字符串
//		String msg=RedisService.beanToString(message);
//		log.info("send message:"+message);
//		
//		//第一个参数队列的名字，第二个参数发出的信息
//		amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);
//	}
//	
//	public void sendTopic(Object message) {
//		//将对象转换为字符串
//		String msg=RedisService.beanToString(message);
//		log.info("sendTopic message:"+message);
//		
//		//exchange routingkey message
//		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.key1",msg+"1");
//		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.key2",msg+"2");
//	}
//	
//	//广播，不需要key
//	public void sendFanout(Object message) {
//		//将对象转换为字符串
//		String msg=RedisService.beanToString(message);
//		log.info("sendFanout message:"+message);
//		
//		//exchange routingkey message
//		amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"",msg+"1");
//		//amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"",msg+"2");
//	}
//	
//	// 使用header，不需要key
//	public void sendHeader(Object message) {
//		// 将对象转换为字符串
//		String msg = RedisService.beanToString(message);
//		log.info("sendHeader message:" + message);
//		MessageProperties properties=new MessageProperties();
//		properties.setHeader("header1", "value1");
//		properties.setHeader("header2", "value2");
//		Message obj=new Message(msg.getBytes(),properties);
//		//传入一个message的对象。
//		amqpTemplate.convertAndSend(MQConfig.HEADER_QUEUE, "", obj);
//	}
}
