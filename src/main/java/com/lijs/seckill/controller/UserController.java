package com.lijs.seckill.controller;

import com.lijs.seckill.domain.SeckillUser;
import com.lijs.seckill.result.Result;
import com.lijs.seckill.service.GoodsService;
import com.lijs.seckill.vo.GoodsVo;
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
    private GoodsService goodsService;

    @RequestMapping("/info")
    @ResponseBody
    public Result<SeckillUser> info(SeckillUser user) {
        return Result.success(user);
    }

    @RequestMapping("/to_detail/{goodsId}")
    public String toDetail(Model model, SeckillUser user, @PathVariable("goodsId") long goodsId) {
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);
        // 秒杀开始时间，结束时间
        long start = goods.getStartDate().getTime();
        long end = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        // 秒杀状态量
        int status;
        // 开始时间倒计时
        int remailSeconds;
        // 查看当前秒杀状态
        if (now < start) { // 秒杀还未开始 倒计时
            status = 0;
            remailSeconds = (int) ((start - now) / 1000);  // 毫秒转为秒
        } else if (now > end) { // 秒杀已经结束
            status = 2;
            remailSeconds = -1;  // 毫秒转为秒
        } else {//秒杀正在进行
            status = 1;
            remailSeconds = 0;  // 毫秒转为秒
        }
        model.addAttribute("status", status);
        model.addAttribute("remailSeconds", remailSeconds);
        return "goods_detail";
    }

}
