package com.lijs.seckill.rabbitmq;

import com.lijs.seckill.service.GoodsService;
import com.lijs.seckill.service.SeckillService;
import com.lijs.seckill.service.OrderService;
import com.lijs.seckill.vo.GoodsVo;
import com.lijs.seckill.domain.SeckillOrder;
import com.lijs.seckill.domain.SeckillUser;
import com.lijs.seckill.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * MQ接收者
 */
@Service
public class MQReceiver {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private OrderService orderService;

    private final Logger logger = LoggerFactory.getLogger(MQReceiver.class);

    // 指定监听的是哪一个queue
    @RabbitListener(queues = MQConfig.SECKILL)
    public void receiveSeckill(String message) {
        logger.info("receive seckill message: {}", message);
        SeckillMessage mm = RedisService.stringToBean(message, SeckillMessage.class);
        SeckillUser user = mm.getUser();
        long goodsId = mm.getGoodsId();
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        int stockCount = goodsVo.getStockCount();
        // 1.判断库存不足
        if (stockCount <= 0) {
            // 注意库存至临界值 1 的时候，此时刚好来了加入 10 个线程，那么库存就会 -10
            return;
        }
        // 2.判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
        SeckillOrder order = orderService.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if (order != null) {
            return;
        }

        // 一个事务原子操作【1.库存减1 2.下订单 3.写入秒杀订单】
        seckillService.seckillWithCache(user, goodsVo);
    }

}
