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
        logger.info("缓存加载完成...");
    }

    /**
     * 生成图片验证码
     *
     * @param model    模型
     * @param user     秒杀用户
     * @param goodsId  商品ID
     * @param response HTTP响应
     * @return 验证码结果
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
     * 获取秒杀路径并验证验证码
     *
     * @param request    HTTP请求
     * @param model      模型
     * @param user       秒杀用户
     * @param goodsId    商品ID
     * @param verifyCode 验证码
     * @return 秒杀路径
     */
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
        // 限定 key 5s 之内只能访问 5 次
        Integer count = redisService.get(AccessKey.access, key, Integer.class);
        if (count == null) {
            redisService.set(AccessKey.access, key, 1);
        } else if (count < 5) {
            redisService.incr(AccessKey.access, key);
        } else {
            // 超过 5 次
            return Result.error(ResultCode.ACCESS_LIMIT);
        }
        // 验证验证码
        boolean check = seckillService.checkVCode(user, goodsId, verifyCode);
        if (!check) {
            return Result.error(ResultCode.REQUEST_ILLEGAL);
        }
        logger.info("通过!");
        // 生成一个随机串
        String path = seckillService.createSeckillPath(user, goodsId);
        logger.info("path:{}", path);
        return Result.success(path);
    }

    /**
     * 轮询查看秒杀结果
     * 秒杀成功，返回订单的Id。
     * 库存不足直接返回-1。
     * 排队中则返回0。
     *
     * @param user    秒杀用户
     * @param goodsId 商品ID
     * @return 秒杀结果
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> result(SeckillUser user, @RequestParam(value = "goodsId", defaultValue = "0") long goodsId) {
        long result = seckillService.getSeckillResult(user.getId(), goodsId);
        logger.info("轮询 result:{}", result);
        return Result.success(result);
    }

    /**
     * 秒杀接口：支持【缓存+消息队列】
     *
     * @param model   模型
     * @param user    秒杀用户
     * @param goodsId 商品ID
     * @param path    秒杀路径
     * @return 秒杀结果
     */
    @RequestMapping(value = "/{path}/seckillWithCacheAndMQ", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> seckillWithCacheAndMQ(Model model, SeckillUser user,
                                                 @RequestParam(value = "goodsId", defaultValue = "0") long goodsId,
                                                 @PathVariable("path") String path) {
        model.addAttribute("user", user);
        // 1.如果用户为空则返回至登录页面
        if (user == null) {
            return Result.error(ResultCode.SESSION_ERROR);
        }
        // 2.验证秒杀path路径
        boolean check = seckillService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(ResultCode.REQUEST_ILLEGAL);
        }
        // 3.Redis 预扣库存（减少数据库访问）
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);
        // 4.判断减少数量 1 之后的库存 stock，区别于查数据库时候的stock <= 0
        if (stock < 0) {
            return Result.error(ResultCode.SECKILL_OVER_ERROR);
        }
        // 5.判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
        SeckillOrder order = orderService.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if (order != null) { // 重复下单
            return Result.error(ResultCode.REPEAT_SECKILL);
        }
        // 6.正常请求入队，发送一个秒杀message到队列里面去，入队之后客户端应该进行轮询。
        SeckillMessage mms = new SeckillMessage();
        mms.setUser(user);
        mms.setGoodsId(goodsId);
        mQSender.sendSeckillMessage(mms);
        // 返回0代表排队中
        return Result.success(0);
    }

    /**
     * 1000*10
     * QPS 703.4822370735138
     * 秒杀接口：未支持【缓存+消息队列】
     * 秒杀操作，直接返回订单详情页
     *
     * @param model   模型
     * @param user    秒杀用户
     * @param goodsId 商品ID
     * @return 订单详情页
     */
    @RequestMapping("/seckillWithoutCache")
    public String seckillWithoutCache(Model model, SeckillUser user, @RequestParam("goodsId") Long goodsId) {
        model.addAttribute("user", user);
        // 如果用户为空则返回至登录页面
        if (user == null) {
            return "login";
        }
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        // 判断商品库存，库存大于0，才进行操作，多线程下会出错
        int stockCount = goodsVo.getStockCount();
        if (stockCount <= 0) {
            model.addAttribute("errorMessage", ResultCode.SECKILL_OVER_ERROR);
            return "seckill_fail";
        }
        // 判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
        SeckillOrder order = orderService.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if (order != null) {//重复下单
            model.addAttribute("errorMessage", ResultCode.REPEAT_SECKILL);
            return "seckill_fail";
        }
        OrderInfo orderinfo = seckillService.seckill(user, goodsVo);
        // 如果秒杀成功则直接跳转到订单详情页
        model.addAttribute("orderinfo", orderinfo);
        model.addAttribute("goods", goodsVo);
        return "order_detail";
    }

}
