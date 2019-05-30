package com.ljs.miaosha.controller;

import com.ljs.miaosha.domain.User;
import com.ljs.miaosha.redis.RedisService;
import com.ljs.miaosha.redis.UserKey;
import com.ljs.miaosha.result.CodeMsg;
import com.ljs.miaosha.result.Result;
import com.ljs.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")//注意！！！加了一个路径
public class DemoController {
	@Autowired
	UserService userService;
	@Autowired
	RedisService redisService;
	
	@RequestMapping("/")
	@ResponseBody
	public String home() {
		return "hello world";
	}
	@RequestMapping("/hello")
	@ResponseBody
	public Result<String> hello() {//0代表成功
		return Result.success("hello sss");
	}
	
	@RequestMapping("/helloError")
	@ResponseBody
	public Result<String> helloError() {//0代表成功
		return Result.error(CodeMsg.SERVER_ERROR);
	}
	//@responseBody注解的作用是将controller的方法返回的对象通过适当的转换器转换为指定的格式之后，写入到response对象的body区，通常用来返回JSON数据或者是XML
	//数据，需要注意的呢，在使用此注解之后不会再走视图处理器，而是直接将数据写入到输入流中，他的效果等同于通过response对象输出指定格式的数据。
	@RequestMapping("/thymeleaf")	//用thymeleaf返回模板，用String返回!!!
	//@ResponseBody		
	//@responsebody表示该方法的返回结果直接写入HTTP response body中。
	public String helloThymeleaf(Model model) {//0代表成功
		model.addAttribute("name", "pitt");
		return "hello";//他会从配置文件里面去找
	}
	
	@RequestMapping("/db/get")
	@ResponseBody
	public Result<User> dbGet() {//0代表成功		
		User user=userService.getById(1);
		System.out.println("res:"+user.getName());
		return Result.success(user);
	}
	
	@RequestMapping("/db/tx")
	@ResponseBody
	public Result<Boolean> dbTx() {//0代表成功		
		
		System.out.println("res:"+userService.tx());
		return Result.success(userService.tx());
	}
	
	@RequestMapping("/redis/get")
	@ResponseBody
	public Result<Long> redisGet() {//0代表成功		
		Long l1=redisService.get("key1",Long.class);
		//redisService.get("key1",String.class);
		//System.out.println("res:"+userService.tx());
		return Result.success(l1);
	}
	@RequestMapping("/redis/get1")
	@ResponseBody
	public Result<String> redisGet1() {//0代表成功		
		String res=redisService.get("key1",String.class);
		//redisService.get("key1",String.class);
		//System.out.println("res:"+userService.tx());
		return Result.success(res);
	}
	
	@RequestMapping("/redis/getbyid")
	@ResponseBody
	public Result<User> redisGetById() {//0代表成功		
		User res=redisService.get(UserKey.getById,""+1,User.class);
		//redisService.get("key1",String.class);
		//System.out.println("res:"+userService.tx());
		return Result.success(res);
	}
	/**
	 *避免key被不同类的数据覆盖 
	 *使用Prefix前缀-->不同类别的缓存，用户、部门、
	 */
	@RequestMapping("/redis/set")
	@ResponseBody
	public Result<Boolean> redisSet() {//0代表成功		
		User user=new User(1,"1111");
		boolean f=redisService.set(UserKey.getById,""+1,user);
		return Result.success(true);
	}
	@RequestMapping("/s")
	@ResponseBody
	public String say() {
		return "hello world";
	}
	
}
