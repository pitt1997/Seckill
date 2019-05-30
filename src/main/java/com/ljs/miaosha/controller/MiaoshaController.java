package com.ljs.miaosha.controller;

import com.ljs.miaosha.domain.MiaoshaOrder;
import com.ljs.miaosha.domain.MiaoshaUser;
import com.ljs.miaosha.domain.OrderInfo;
import com.ljs.miaosha.rabbitmq.MQSender;
import com.ljs.miaosha.rabbitmq.MiaoshaMessage;
import com.ljs.miaosha.redis.AccessKey;
import com.ljs.miaosha.redis.GoodsKey;
import com.ljs.miaosha.redis.RedisService;
import com.ljs.miaosha.result.CodeMsg;
import com.ljs.miaosha.result.Result;
import com.ljs.miaosha.service.GoodsService;
import com.ljs.miaosha.service.MiaoshaService;
import com.ljs.miaosha.service.MiaoshaUserService;
import com.ljs.miaosha.service.OrderService;
import com.ljs.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/miaosha")
@Controller
public class MiaoshaController implements InitializingBean{
	@Autowired
	GoodsService goodsService;
	@Autowired
	RedisService redisService;
	@Autowired
	MiaoshaUserService miaoshaUserService;
	//作为秒杀功能事务的Service
	@Autowired
	MiaoshaService miaoshaService;
	@Autowired
	OrderService orderService;
	@Autowired
	MQSender mQSender;
	
	//标记
	Map <Long,Boolean>localMap=new HashMap<Long,Boolean>();
	/**
	 * 系统初始化的时候做的事情。
	 * 在容器启动时候，检测到了实现了接口InitializingBean之后，
	 */
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodslist=goodsService.getGoodsVoList();
		if(goodslist==null) {
			return;
		}
		for(GoodsVo goods:goodslist) {
			//如果不是null的时候，将库存加载到redis里面去 prefix---GoodsKey:gs ,	 key---商品id,	 value
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), goods.getStockCount());
		}
	}
	
	/**
	 * 生成图片验证码
	 */
	@RequestMapping(value ="/vertifyCode")
	@ResponseBody
	public Result<String> getVertifyCode(Model model, MiaoshaUser user,
										 @RequestParam("goodsId") Long goodsId, HttpServletResponse response) {
		model.addAttribute("user", user);
		//如果用户为空，则返回至登录页面
		if(user==null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		BufferedImage img=miaoshaService.createMiaoshaVertifyCode(user, goodsId);
		try {
			OutputStream out=response.getOutputStream();
			ImageIO.write(img,"JPEG", out);
			out.flush();
			out.close();
			return null; 
		} catch (IOException e) {
			e.printStackTrace();
			return Result.error(CodeMsg.MIAOSHA_FAIL);
		}
	}
	
	/**
	 * 获取秒杀的path,并且验证验证码的值是否正确
	 */
	//@AccessLimit(seconds=5,maxCount=5,needLogin=true)
	//加入注解，实现拦截功能，进而实现限流功能
	//@AccessLimit(seconds=5,maxCount=5,needLogin=true)
	@RequestMapping(value ="/getPath")
	@ResponseBody
	public Result<String> getMiaoshaPath(HttpServletRequest request,Model model,MiaoshaUser user,
			@RequestParam("goodsId") Long goodsId,
			@RequestParam(value="vertifyCode",defaultValue="0") int vertifyCode) {
		model.addAttribute("user", user);
		//如果用户为空，则返回至登录页面
		if(user==null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//限制访问次数
		String uri=request.getRequestURI();
		String key=uri+"_"+user.getId();
		//限定key5s之内只能访问5次
		Integer count=redisService.get(AccessKey.access, key, Integer.class);
		if(count==null) {
			redisService.set(AccessKey.access, key, 1);
		}else if(count<5) {
			redisService.incr(AccessKey.access, key);
		}else {//超过5次
			return Result.error(CodeMsg.ACCESS_LIMIT);
		}
		
		//验证验证码
		boolean check=miaoshaService.checkVCode(user, goodsId,vertifyCode );
		if(!check) {
			return Result.error(CodeMsg.REQUEST_ILLEAGAL);
		}
		System.out.println("通过!");
		//生成一个随机串
		String path=miaoshaService.createMiaoshaPath(user,goodsId);
		System.out.println("@MiaoshaController-tomiaoshaPath-path:"+path);
		return Result.success(path); 
	}
	
	/**
	 * 客户端做一个轮询，查看是否成功与失败，失败了则不用继续轮询。
	 * 秒杀成功，返回订单的Id。
	 * 库存不足直接返回-1。
	 * 排队中则返回0。
	 * 查看是否生成秒杀订单。
	 */
	@RequestMapping(value = "/result", method = RequestMethod.GET)
	@ResponseBody
	public Result<Long> doMiaoshaResult(Model model, MiaoshaUser user,
			@RequestParam(value = "goodsId", defaultValue = "0") long goodsId) {
		long result=miaoshaService.getMiaoshaResult(user.getId(),goodsId);
		System.out.println("轮询 result："+result);
		return Result.success(result);
	}
	
	
	/**
	 * 563.1899076368552
	 * 做缓存+消息队列
	 * 1.系统初始化，把商品库存数量加载到Redis上面来。
	 * 2.收到请求，Redis预减库存。
	 * 3.请求入队，立即返回排队中。
	 * 4.请求出队，生成订单，减少库存（事务）。
	 * 5.客户端轮询，是否秒杀成功。
	 * 
	 * 不能是GET请求，GET
	 */
	//POST请求 
	@RequestMapping(value="/{path}/do_miaosha_ajaxcache",method=RequestMethod.POST)
	@ResponseBody
	public Result<Integer> doMiaoshaCache(Model model,MiaoshaUser user,
			@RequestParam(value="goodsId",defaultValue="0") long goodsId,
			@PathVariable("path")String path) {
		model.addAttribute("user", user);
		//1.如果用户为空，则返回至登录页面
		if(user==null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//验证path,去redis里面取出来然后验证。
		boolean check=miaoshaService.checkPath(user,goodsId,path);
		if(!check) {
			return Result.error(CodeMsg.REQUEST_ILLEAGAL);
		}
		//内存标记，减少对redis的访问 localMap.put(goodsId,false);
//		boolean over=localMap.get(goodsId);
//		//在容量满的时候，那么就打标记为true
//		if(over) {
//			return Result.error(CodeMsg.MIAOSHA_OVER_ERROR);
//		}
		//2.预减少库存，减少redis里面的库存
		long stock=redisService.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId);
		//3.判断减少数量1之后的stock，区别于查数据库时候的stock<=0
		if(stock<0) {//线程不安全---失败			库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
			return Result.error(CodeMsg.MIAOSHA_OVER_ERROR);
		}
		//4.判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndCoodsId(user.getId(), goodsId);
		if (order != null) {// 重复下单
			// model.addAttribute("errorMessage", CodeMsg.REPEATE_MIAOSHA);
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		//5.正常请求，入队，发送一个秒杀message到队列里面去，入队之后客户端应该进行轮询。
		MiaoshaMessage mms=new MiaoshaMessage();
		mms.setUser(user);
		mms.setGoodsId(goodsId);
		mQSender.sendMiaoshaMessage(mms);
		//返回0代表排队中
		return Result.success(0);
	}
	/**
	 * 1000*10
	 * QPS 703.4822370735138
	 * @param model
	 * @param user
	 * @return
	 */
	@RequestMapping("/do_miaosha")//传入user对象啊，不然怎么取user的值，${user.nickname}
	public String toList(Model model,MiaoshaUser user,@RequestParam("goodsId") Long goodsId) {
		model.addAttribute("user", user);
		//如果用户为空，则返回至登录页面
		if(user==null){
			return "login";
		}
		GoodsVo goodsvo=goodsService.getGoodsVoByGoodsId(goodsId);
		//判断商品库存，库存大于0，才进行操作，多线程下会出错
		int  stockcount=goodsvo.getStockCount();		
		if(stockcount<=0) {//失败			库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
			model.addAttribute("errorMessage", CodeMsg.MIAOSHA_OVER_ERROR);
			return "miaosha_fail";
		}
		//判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品 
		MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdAndCoodsId(user.getId(),goodsId);
		if(order!=null) {//重复下单
			model.addAttribute("errorMessage", CodeMsg.REPEATE_MIAOSHA);
			return "miaosha_fail";
		}
		//可以秒杀，原子操作：1.库存减1，2.下订单，3.写入秒杀订单--->是一个事务
		OrderInfo orderinfo=miaoshaService.miaosha(user,goodsvo);
		//如果秒杀成功，直接跳转到订单详情页上去。
		model.addAttribute("orderinfo", orderinfo);
		model.addAttribute("goods", goodsvo);
		return "order_detail";//返回页面login
	}
	
	
	/**
	 * 
	 * 做了页面静态化的，直接返回订单的信息
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 * 
	 * 不能是GET请求，GET，
	 */
	//POST请求 
	@RequestMapping(value="/do_miaosha_ajax",method=RequestMethod.POST)
	@ResponseBody
	public Result<OrderInfo> doMiaosha(Model model,MiaoshaUser user,@RequestParam(value="goodsId",defaultValue="0") long goodsId) {
		model.addAttribute("user", user);
		System.out.println("do_miaosha_ajax");
		System.out.println("goodsId:"+goodsId);
		//如果用户为空，则返回至登录页面
		if(user==null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		GoodsVo goodsvo=goodsService.getGoodsVoByGoodsId(goodsId);
		//判断商品库存，库存大于0，才进行操作，多线程下会出错
		int  stockcount=goodsvo.getStockCount();		
		if(stockcount<=0) {//失败			库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
			//model.addAttribute("errorMessage", CodeMsg.MIAOSHA_OVER_ERROR);
			return Result.error(CodeMsg.MIAOSHA_OVER_ERROR);
		}
		//判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品 
		MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdAndCoodsId(user.getId(),goodsId);
		if(order!=null) {//重复下单
			//model.addAttribute("errorMessage", CodeMsg.REPEATE_MIAOSHA);
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		//可以秒杀，原子操作：1.库存减1，2.下订单，3.写入秒杀订单--->是一个事务
		OrderInfo orderinfo=miaoshaService.miaosha(user,goodsvo);
		//如果秒杀成功，直接跳转到订单详情页上去。
		model.addAttribute("orderinfo", orderinfo);
		model.addAttribute("goods", goodsvo);
		return Result.success(orderinfo);
	}
	
}
