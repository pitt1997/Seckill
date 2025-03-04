package com.lijs.seckill.controller;

import com.lijs.seckill.domain.User;
import com.lijs.seckill.redis.RedisService;
import com.lijs.seckill.redis.UserKey;
import com.lijs.seckill.result.Result;
import com.lijs.seckill.result.ResultCode;
import com.lijs.seckill.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {

    private final Logger logger = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;

    @RequestMapping("/")
    @ResponseBody
    public String home() {
        return "hello world";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello() {
        return Result.success("hello");
    }

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> helloError() {//0代表成功
        return Result.error(ResultCode.SERVER_ERROR);
    }

    /**
     * @responseBody注解的作用是将controller的方法返回的对象通过适当的转换器转换为指定的格式之后， 写入到response对象的body区，通常用来返回JSON数据或者是XML数据，需要注意在使用此注解之后不会
     * 再走视图处理器，而是直接将数据写入到输入流中，他的效果等同于通过response对象输出指定格式的数据。
     */
    @RequestMapping("/thymeleaf")
    public String helloThymeleaf(Model model) {
        model.addAttribute("name", "pitt1997");
        return "hello"; // 他会从配置文件里面去找
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet() {
        User user = userService.getById(1);
        logger.info("username:{}", user.getName());
        return Result.success(user);
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx() {
        logger.info("tx:{}", userService.tx());
        return Result.success(userService.tx());
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<Long> redisGet() {
        Long l1 = redisService.get("key1", Long.class);
        return Result.success(l1);
    }

    @RequestMapping("/redis/get1")
    @ResponseBody
    public Result<String> redisGet1() {
        String res = redisService.get("key1", String.class);
        return Result.success(res);
    }

    @RequestMapping("/redis/getById")
    @ResponseBody
    public Result<User> redisGetById() {
        User res = redisService.get(UserKey.getById, "" + 1, User.class);
        return Result.success(res);
    }

    /**
     * 避免key被不同类的数据覆盖
     * 使用Prefix前缀-->不同类别的缓存，用户、部门、
     */
    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user = new User(1, "1111");
        boolean f = redisService.set(UserKey.getById, "" + 1, user);
        return Result.success(true);
    }

}
