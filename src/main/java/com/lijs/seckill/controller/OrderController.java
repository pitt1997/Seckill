package com.lijs.seckill.controller;

import com.lijs.seckill.domain.OrderInfo;
import com.lijs.seckill.domain.SeckillUser;
import com.lijs.seckill.result.Result;
import com.lijs.seckill.result.ResultCode;
import com.lijs.seckill.service.GoodsService;
import com.lijs.seckill.service.OrderService;
import com.lijs.seckill.vo.GoodsVo;
import com.lijs.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/order")
@Controller
public class OrderController {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(SeckillUser user, @RequestParam("orderId") long orderId) {
        if (user == null) {
            return Result.error(ResultCode.SESSION_ERROR);
        }
        OrderInfo order = orderService.getOrderByOrderId(orderId);
        if (order == null) {
            return Result.error(ResultCode.ORDER_NOT_EXIST);
        }
        // 订单存在
        long goodsId = order.getGoodsId();
        GoodsVo gVo = goodsService.getGoodsVoByGoodsId(goodsId);
        OrderDetailVo oVo = new OrderDetailVo();
        oVo.setGoodsVo(gVo);
        oVo.setOrder(order);
        return Result.success(oVo);
    }
}
