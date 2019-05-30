package com.ljs.miaosha.controller;

import com.ljs.miaosha.rabbitmq.MQSender;
import com.ljs.miaosha.redis.RedisService;
import com.ljs.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sample")//注意！！！加了一个路径
public class MQueueController {
	@Autowired
	UserService userService;
	@Autowired
	RedisService redisService;
	@Autowired
	MQSender sender;
	
	
//	@RequestMapping("/mq")
//	@ResponseBody
//	public Result<String> mq() {//0代表成功
//		String message="hello ljs!";
//		sender.send(message);
//		
//		return Result.success("hello sss");
//	}
//	
//	@RequestMapping("/mq/topic")
//	@ResponseBody
//	public Result<String> mqtopic() {//0代表成功
//		String message="hello t ljs!";
//		sender.sendTopic(message);
//		
//		return Result.success("hello ljs!");
//	}
//	
//	@RequestMapping("/mq/fanout")
//	@ResponseBody
//	public Result<String> mqfanout() {//0代表成功
//		String message="hello tff ljs!";
//		sender.sendFanout(message);
//		
//		return Result.success("hello ljs!");
//	}
//	
//	@RequestMapping("/mq/header")
//	@ResponseBody
//	public Result<String> mqheader() {//0代表成功
//		String message="hello header ljs!";
//		sender.sendHeader(message);
//		
//		return Result.success("hello ljs!");
//	}
	
}
