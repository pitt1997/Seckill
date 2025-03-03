package com.lijs.seckill.controller;

import com.lijs.seckill.domain.OrderInfo;
import com.lijs.seckill.domain.SeckillOrder;
import com.lijs.seckill.domain.SeckillUser;
import com.lijs.seckill.rabbitmq.MQSender;
import com.lijs.seckill.rabbitmq.SeckillMessage;
import com.lijs.seckill.redis.AccessKey;
import com.lijs.seckill.redis.GoodsKey;
import com.lijs.seckill.redis.RedisService;
import com.lijs.seckill.result.Result;
import com.lijs.seckill.result.ResultCode;
import com.lijs.seckill.service.GoodsService;
import com.lijs.seckill.service.OrderService;
import com.lijs.seckill.service.SeckillService;
import com.lijs.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;

@RequestMapping("/seckill")
@Controller
public class SeckillController implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(SeckillController.class);

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MQSender mQSender;

    /**
     * 系统初始化的时候做的事情。
     * 在容器启动时候，检测到了实现了接口InitializingBean之后，
     */
    public void afterPropertiesSet() {
        List<GoodsVo> goodslist = goodsService.getGoodsVoList();
        if (goodslist == null) {
            return;
        }
        for (GoodsVo goods : goodslist) {
            // 程序启动时将商品库存加载到redis中
            redisService.set(GoodsKey.getSeckillGoodsStock, "" + goods.getId(), goods.getStockCount());
        }
    }

    /**
     * 生成图片验证码
     */
    @RequestMapping(value = "/verifyCode")
    @ResponseBody
    public Result<String> verifyCode(Model model, SeckillUser user,
                                     @RequestParam("goodsId") Long goodsId, HttpServletResponse response) {
        model.addAttribute("user", user);
        // 如果用户为空则返回登录页面
        if (user == null) {
            return Result.error(ResultCode.SESSION_ERROR);
        }
        BufferedImage img = seckillService.createSeckillVerifyCode(user, goodsId);
        try {
            OutputStream out = response.getOutputStream();
            ImageIO.write(img, "JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return Result.error(ResultCode.SECKILL_FAIL);
        }
    }

    /**
     * 获取秒杀的path, 并且验证验证码的值是否正确
     */
    // @AccessLimit(seconds = 5, maxCount = 5, needLogin = true) 拦截实现限流功能
    @RequestMapping(value = "/getPath")
    @ResponseBody
    public Result<String> getSeckillPath(HttpServletRequest request, Model model, SeckillUser user,
                                         @RequestParam("goodsId") Long goodsId,
                                         @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode) {
        model.addAttribute("user", user);
        // 如果用户为空，则返回至登录页面
        if (user == null) {
            return Result.error(ResultCode.SESSION_ERROR);
        }
        // 限制访问次数
        String uri = request.getRequestURI();
        String key = uri + "_" + user.getId();
        // 限定key5s之内只能访问5次
        Integer count = redisService.get(AccessKey.access, key, Integer.class);
        if (count == null) {
            redisService.set(AccessKey.access, key, 1);
        } else if (count < 5) {
            redisService.incr(AccessKey.access, key);
        } else {
            // 超过5次
            return Result.error(ResultCode.ACCESS_LIMIT);
        }
        // 验证验证码
        boolean check = seckillService.checkVCode(user, goodsId, verifyCode);
        if (!check) {
            return Result.error(ResultCode.REQUEST_ILLEGAL);
        }
        logger.info("通过!");
        // 生成一个随机串
        String path = seckillService.createMiaoshaPath(user, goodsId);
        logger.info("path:{}", path);
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
    public Result<Long> result(SeckillUser user, @RequestParam(value = "goodsId", defaultValue = "0") long goodsId) {
        long result = seckillService.getMiaoshaResult(user.getId(), goodsId);
        System.out.println("轮询 result：" + result);
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
     * <p>
     * 不能是GET请求，GET
     */
    //POST请求 
    @RequestMapping(value = "/{path}/do_miaosha_ajaxcache", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> doMiaoshaCache(Model model, SeckillUser user,
                                          @RequestParam(value = "goodsId", defaultValue = "0") long goodsId,
                                          @PathVariable("path") String path) {
        model.addAttribute("user", user);
        // 1.如果用户为空，则返回至登录页面
        if (user == null) {
            return Result.error(ResultCode.SESSION_ERROR);
        }
        // 验证path, 去redis里面取出来然后验证。
        boolean check = seckillService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(ResultCode.REQUEST_ILLEGAL);
        }
        // 2.预减少库存，减少redis里面的库存
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);
        // 3.判断减少数量1之后的stock，区别于查数据库时候的stock<=0
        if (stock < 0) {
            return Result.error(ResultCode.SECKILL_OVER_ERROR);
        }
        // 4.判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
        SeckillOrder order = orderService.getMiaoshaOrderByUserIdAndCoodsId(user.getId(), goodsId);
        if (order != null) {// 重复下单
            // model.addAttribute("errorMessage", CodeMsg.REPEATE_MIAOSHA);
            return Result.error(ResultCode.REPEAT_SECKILL);
        }
        // 5.正常请求，入队，发送一个秒杀message到队列里面去，入队之后客户端应该进行轮询。
        SeckillMessage mms = new SeckillMessage();
        mms.setUser(user);
        mms.setGoodsId(goodsId);
        mQSender.sendMiaoshaMessage(mms);
        // 返回0代表排队中
        return Result.success(0);
    }

    /**
     * 1000*10
     * QPS 703.4822370735138
     */
    @RequestMapping("/do_miaosha")
    public String toList(Model model, SeckillUser user, @RequestParam("goodsId") Long goodsId) {
        model.addAttribute("user", user);
        //如果用户为空，则返回至登录页面
        if (user == null) {
            return "login";
        }
        GoodsVo goodsvo = goodsService.getGoodsVoByGoodsId(goodsId);
        //判断商品库存，库存大于0，才进行操作，多线程下会出错
        int stockCount = goodsvo.getStockCount();
        if (stockCount <= 0) {//失败			库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
            model.addAttribute("errorMessage", ResultCode.SECKILL_OVER_ERROR);
            return "seckill_fail";
        }
        //判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品 
        SeckillOrder order = orderService.getMiaoshaOrderByUserIdAndCoodsId(user.getId(), goodsId);
        if (order != null) {//重复下单
            model.addAttribute("errorMessage", ResultCode.REPEAT_SECKILL);
            return "seckill_fail";
        }
        //可以秒杀，原子操作：1.库存减1，2.下订单，3.写入秒杀订单--->是一个事务
        OrderInfo orderinfo = seckillService.miaosha(user, goodsvo);
        //如果秒杀成功，直接跳转到订单详情页上去。
        model.addAttribute("orderinfo", orderinfo);
        model.addAttribute("goods", goodsvo);
        return "order_detail";//返回页面login
    }


    /**
     * 做了页面静态化的，直接返回订单的信息
     *
     * @param model
     * @param user
     * @param goodsId
     * @return 不能是GET请求，GET，
     */
    //POST请求 
    @RequestMapping(value = "/do_miaosha_ajax", method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> doMiaosha(Model model, SeckillUser user, @RequestParam(value = "goodsId", defaultValue = "0") long goodsId) {
        model.addAttribute("user", user);
        System.out.println("do_miaosha_ajax");
        System.out.println("goodsId:" + goodsId);
        //如果用户为空，则返回至登录页面
        if (user == null) {
            return Result.error(ResultCode.SESSION_ERROR);
        }
        GoodsVo goodsvo = goodsService.getGoodsVoByGoodsId(goodsId);
        //判断商品库存，库存大于0，才进行操作，多线程下会出错
        int stockcount = goodsvo.getStockCount();
        if (stockcount <= 0) {//失败			库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
            //model.addAttribute("errorMessage", CodeMsg.MIAOSHA_OVER_ERROR);
            return Result.error(ResultCode.SECKILL_OVER_ERROR);
        }
        //判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品 
        SeckillOrder order = orderService.getMiaoshaOrderByUserIdAndCoodsId(user.getId(), goodsId);
        if (order != null) {//重复下单
            //model.addAttribute("errorMessage", CodeMsg.REPEATE_MIAOSHA);
            return Result.error(ResultCode.REPEAT_SECKILL);
        }
        //可以秒杀，原子操作：1.库存减1，2.下订单，3.写入秒杀订单--->是一个事务
        OrderInfo orderinfo = seckillService.miaosha(user, goodsvo);
        //如果秒杀成功，直接跳转到订单详情页上去。
        model.addAttribute("orderinfo", orderinfo);
        model.addAttribute("goods", goodsvo);
        return Result.success(orderinfo);
    }

}
