package com.ljs.miaosha.controller;

import com.ljs.miaosha.domain.MiaoshaUser;
import com.ljs.miaosha.redis.RedisService;
import com.ljs.miaosha.result.Result;
import com.ljs.miaosha.service.GoodsService;
import com.ljs.miaosha.service.MiaoshaUserService;
import com.ljs.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/user")
@Controller
public class UserController {
	@Autowired
	GoodsService goodsService;
	@Autowired
	RedisService redisService;
	@Autowired
	MiaoshaUserService miaoshaUserService;
	
	
	
	
	@RequestMapping("/info") 
	@ResponseBody
	public Result<MiaoshaUser> info(Model model, MiaoshaUser user) {
		return Result.success(user);//返回页面login
	}
	
	
	
	
	@RequestMapping("/to_detail/{goodsId}")
	public String toDetail(Model model,MiaoshaUser user,@PathVariable("goodsId")long goodsId) {//id一般用snowflake算法
		model.addAttribute("user", user);
		GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goods);
		//既然是秒杀，还要传入秒杀开始时间，结束时间等信息
		long start=goods.getStartDate().getTime();
		long end=goods.getEndDate().getTime();
		long now=System.currentTimeMillis();
		//秒杀状态量
		int status=0;
		//开始时间倒计时
		int remailSeconds=0;
		//查看当前秒杀状态
		if(now<start) {//秒杀还未开始，--->倒计时
			status=0;
			remailSeconds=(int) ((start-now)/1000);  //毫秒转为秒
		}else if(now>end){ //秒杀已经结束
			status=2;
			remailSeconds=-1;  //毫秒转为秒
		}else {//秒杀正在进行
			status=1;
			remailSeconds=0;  //毫秒转为秒
		}
		model.addAttribute("status", status);
		model.addAttribute("remailSeconds", remailSeconds);
		return "goods_detail";//返回页面login
	}
	
	
}
