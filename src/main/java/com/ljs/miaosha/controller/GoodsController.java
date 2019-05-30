package com.ljs.miaosha.controller;

import com.ljs.miaosha.domain.MiaoshaUser;
import com.ljs.miaosha.redis.GoodsKey;
import com.ljs.miaosha.redis.RedisService;
import com.ljs.miaosha.result.Result;
import com.ljs.miaosha.service.GoodsService;
import com.ljs.miaosha.service.MiaoshaUserService;
import com.ljs.miaosha.vo.GoodsDetailVo;
import com.ljs.miaosha.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/goods")
@Controller
public class GoodsController {
	@Autowired
	GoodsService goodsService;
	@Autowired
	RedisService redisService;
	@Autowired
	MiaoshaUserService miaoshaUserService;
	//注入渲染
	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;
	@Autowired
	ApplicationContext applicationContext;
	/**
	 * 未作页面缓存
	 * 1000*10
	 * QPS 784.9293563579279
	 * @param model
	 * @param user
	 * @return
	 */
	@RequestMapping("/to_list_noCache")//传入user对象啊，不然怎么取user的值，${user.nickname}
	public String toListnoCache(Model model,MiaoshaUser user) {
		model.addAttribute("user", user);
		//查询商品列表
		List<GoodsVo> goodsList= goodsService.getGoodsVoList();
		model.addAttribute("goodsList", goodsList);
		return "goods_list";//返回页面login
	}
	/**
	 * 做页面缓存的list页面，防止同一时间访问量巨大到达数据库，如果缓存时间过长，数据及时性就不高。
	 * 
	 * 1000*10
	 * QPS 1201.923076923077
	 */
	//5-17
	@RequestMapping(value="/to_list",produces="text/html")//传入user对象啊，不然怎么取user的值，${user.nickname}
	@ResponseBody
	public String toListCache(Model model,MiaoshaUser user,HttpServletRequest request,
			HttpServletResponse response) {
		// 1.取缓存
		// public <T> T get(KeyPrefix prefix,String key,Class<T> data)
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if (!StringUtils.isEmpty(html)) {
			return html;
		}
		model.addAttribute("user", user);
		//查询商品列表
		List<GoodsVo> goodsList= goodsService.getGoodsVoList();
		model.addAttribute("goodsList", goodsList);
		
		//2.手动渲染  使用模板引擎	templateName:模板名称 	String templateName="goods_list";
		SpringWebContext context=new SpringWebContext(request,response,request.getServletContext(),
				request.getLocale(),model.asMap(),applicationContext);
		html=thymeleafViewResolver.getTemplateEngine().process("goods_list", context);
		//保存至缓存
		if(!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsList, "", html);//key---GoodsKey:gl---缓存goodslist这个页面
		}
		return html;
		//return "goods_list";//返回页面login
	}
	/**
	 * 做了页面缓存的to_detail商品详情页。
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value="/to_detail_html/{goodsId}")  //produces="text/html"
	@ResponseBody
	public String toDetailCachehtml(Model model,MiaoshaUser user,
			HttpServletRequest request,HttpServletResponse response,@PathVariable("goodsId")long goodsId) {//id一般用snowflake算法
		// 1.取缓存
		// public <T> T get(KeyPrefix prefix,String key,Class<T> data)
		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);//不同商品页面不同的详情
		if (!StringUtils.isEmpty(html)) {
			return html;
		}
		//缓存中没有，则将业务数据取出，放到缓存中去。
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
		
		// 2.手动渲染 使用模板引擎 templateName:模板名称 String templateName="goods_detail";
		SpringWebContext context = new SpringWebContext(request, response, request.getServletContext(),
				request.getLocale(), model.asMap(), applicationContext);
		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", context);
		// 将渲染好的html保存至缓存
		if (!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
		}
		return html;//html是已经渲染好的html文件
		//return "goods_detail";//返回页面login
	}
	
	
	/**
	 * 作页面静态化的商品详情
	 * 页面存的是html
	 * 动态数据通过接口从服务端获取
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value="/detail/{goodsId}")  //produces="text/html"
	@ResponseBody
	public Result<GoodsDetailVo> toDetail_staticPage(Model model, MiaoshaUser user,
													 HttpServletRequest request, HttpServletResponse response, @PathVariable("goodsId")long goodsId) {//id一般用snowflake算法
		System.out.println("页面静态化/detail/{goodsId}");
		model.addAttribute("user", user);
		GoodsVo goodsVo=goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goodsVo);
		//既然是秒杀，还要传入秒杀开始时间，结束时间等信息
		long start=goodsVo.getStartDate().getTime();
		long end=goodsVo.getEndDate().getTime();
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
		GoodsDetailVo gdVo=new GoodsDetailVo();
		gdVo.setGoodsVo(goodsVo);
		gdVo.setStatus(status);
		gdVo.setRemailSeconds(remailSeconds);
		gdVo.setUser(user);
		//将数据填进去，传至页面
		return Result.success(gdVo);		
	}
	
	
	
	/**
	 * 未作页面缓存
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping("/to_detail_noChache/{goodsId}")
	public String toDetailnoChache(Model model,MiaoshaUser user,@PathVariable("goodsId")long goodsId) {//id一般用snowflake算法
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
	
	
	/**
	 * 之前的版本  1.0  未作user的参数，即未作UserArgumentResolver时调用的detail请求
	 * @param model
	 * @param cookieToken
	 * @param response
	 * @return
	 */
	
	@RequestMapping("/to_detail1")//传入user对象啊，不然怎么取user的值，${user.nickname}
	public String toDetail(Model model,@CookieValue(value=MiaoshaUserService.COOKIE1_NAME_TOKEN)String cookieToken
			,HttpServletResponse response) {
		//通过取到cookie，首先取@RequestParam没有再去取@CookieValue
		if(StringUtils.isEmpty(cookieToken)) {
			return "login";//返回到登录界面
		}
		String token=cookieToken;
		System.out.println("goods-token:"+token);
		System.out.println("goods-cookieToken:"+cookieToken);		
		MiaoshaUser user=miaoshaUserService.getByToken(token,response);
		model.addAttribute("user", user);
		return "goods_list";//返回页面login
	}
	@RequestMapping("/to_list1")//传入user对象啊，不然怎么取user的值，${user.nickname}
	public String toList(Model model,@CookieValue(value=MiaoshaUserService.COOKIE1_NAME_TOKEN)String cookieToken
			,HttpServletResponse response) {
		//通过取到cookie，首先取@RequestParam没有再去取@CookieValue
		if(StringUtils.isEmpty(cookieToken)) {
			return "login";//返回到登录界面
		}
		String token=cookieToken;
		System.out.println("goods-token:"+token);
		System.out.println("goods-cookieToken:"+cookieToken);
		//!!!miaoshaUserService.getByToken(token,response);
		MiaoshaUser user=miaoshaUserService.getByToken(token,response);
		model.addAttribute("user", user);
		return "goods_list";//返回页面login
	}
	
	@RequestMapping("/to_list2")//传入user对象啊，不然怎么取user的值，${user.nickname}
	public String toLogin(Model model,@CookieValue(value=MiaoshaUserService.COOKIE1_NAME_TOKEN)String cookieToken,
			@RequestParam(value=MiaoshaUserService.COOKIE1_NAME_TOKEN)String paramToken,HttpServletResponse response) {
		//通过取到cookie，首先取@RequestParam没有再去取@CookieValue
		if(StringUtils.isEmpty(paramToken)&&StringUtils.isEmpty(cookieToken)) {
			return "login";//返回到登录界面
		}
		String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		System.out.println("goods-token:"+token);
		System.out.println("goods-cookieToken:"+cookieToken);
		System.out.println("goods-paramToken:"+paramToken);
		MiaoshaUser user=miaoshaUserService.getByToken(token,response);
		model.addAttribute("user", user);
		return "goods_list";//返回页面login
	}
}
