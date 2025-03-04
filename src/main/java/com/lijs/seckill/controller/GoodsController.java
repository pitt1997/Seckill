package com.lijs.seckill.controller;

import com.lijs.seckill.domain.SeckillUser;
import com.lijs.seckill.redis.GoodsKey;
import com.lijs.seckill.redis.RedisService;
import com.lijs.seckill.result.Result;
import com.lijs.seckill.service.GoodsService;
import com.lijs.seckill.vo.GoodsDetailVo;
import com.lijs.seckill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/goods")
@Controller
public class GoodsController {

    private final Logger logger = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TemplateEngine templateEngine; // 自动配置的 TemplateEngine
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 未作页面缓存
     * 1000*10
     * QPS 784.9293563579279
     */
    @RequestMapping("/listWithoutCache")
    public String listWithoutCache(Model model, SeckillUser user) {
        model.addAttribute("user", user);
        // 查询商品列表
        List<GoodsVo> goodsList = goodsService.getGoodsVoList();
        model.addAttribute("goodsList", goodsList);
        return "goods_list";
    }

    /**
     * 做页面缓存的list页面，防止同一时间访问量巨大到达数据库，如果缓存时间过长，数据及时性就不高。
     * 1000*10
     * QPS 1201.923076923077
     */
    @RequestMapping(value = "/list", produces = "text/html")
    @ResponseBody
    public String goodsListWithCache(Model model, SeckillUser user, HttpServletRequest request,
                                     HttpServletResponse response) {
        // 1. 先从缓存取 HTML 页面
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        model.addAttribute("user", user);
        List<GoodsVo> goodsList = goodsService.getGoodsVoList();
        model.addAttribute("goodsList", goodsList);

        // 2. 进行手动渲染
        WebContext context = new WebContext(request, response, request.getServletContext(),
                request.getLocale(), model.asMap());
        // 使用 TemplateEngine 处理模板
        html = templateEngine.process("goods_list", context);
        // 保存至缓存
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        return html;
    }

    /**
     * 做了页面缓存的to_detail商品详情页。
     */
    @RequestMapping(value = "/detail/{goodsId}")  //produces="text/html"
    @ResponseBody
    public String goodsDetailCache(Model model, SeckillUser user,
                                   HttpServletRequest request, HttpServletResponse response, @PathVariable("goodsId") long goodsId) {//id一般用snowflake算法
        // 1.取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail, "" + goodsId, String.class);//不同商品页面不同的详情
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        // 缓存中没有，则将业务数据取出，放到缓存中去。
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);
        // 既然是秒杀，还要传入秒杀开始时间，结束时间等信息
        long start = goods.getStartDate().getTime();
        long end = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        // 秒杀状态量
        int status;
        // 开始时间倒计时
        int remainingSeconds;
        // 查看当前秒杀状态
        if (now < start) { // 秒杀还未开始 -> 倒计时
            status = 0;
            remainingSeconds = (int) ((start - now) / 1000);
        } else if (now > end) { // 秒杀已经结束
            status = 2;
            remainingSeconds = -1;
        } else { // 秒杀正在进行
            status = 1;
            remainingSeconds = 0;
        }
        model.addAttribute("status", status);
        model.addAttribute("remainingSeconds", remainingSeconds);

        // 2. 进行手动渲染
        WebContext context = new WebContext(request, response, request.getServletContext(),
                request.getLocale(), model.asMap());
        // 使用 TemplateEngine 处理模板
        html = templateEngine.process("goods_detail", context);

        // 将渲染好的html保存至缓存
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
        }
        // 已经渲染好的html文件
        return html;
    }


    /**
     * 作页面静态化的商品详情
     * 页面存的是html
     * 动态数据通过接口从服务端获取
     */
    @RequestMapping(value = "/detailStatic/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detailStaticPage(Model model, SeckillUser user,
                                                  HttpServletRequest request, HttpServletResponse response, @PathVariable("goodsId") long goodsId) {//id一般用snowflake算法
        logger.info("页面静态化/detail/{goodsId}");

        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goodsVo);
        // 既然是秒杀，还要传入秒杀开始时间，结束时间等信息
        long start = goodsVo.getStartDate().getTime();
        long end = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();
        // 秒杀状态量
        int status;
        // 开始时间倒计时
        int remainingSeconds;
        // 查看当前秒杀状态
        if (now < start) { // 秒杀还未开始 倒计时
            status = 0;
            remainingSeconds = (int) ((start - now) / 1000);  // 毫秒转为秒
        } else if (now > end) { // 秒杀已经结束
            status = 2;
            remainingSeconds = -1;
        } else { // 秒杀正在进行
            status = 1;
            remainingSeconds = 0;
        }
        model.addAttribute("status", status);
        model.addAttribute("remainingSeconds", remainingSeconds);
        GoodsDetailVo gdVo = new GoodsDetailVo();
        gdVo.setGoodsVo(goodsVo);
        gdVo.setStatus(status);
        gdVo.setremainingSeconds(remainingSeconds);
        gdVo.setUser(user);
        // 将数据填进去，传至页面
        return Result.success(gdVo);
    }

}
