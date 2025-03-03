package com.lijs.seckill.service;

import com.lijs.seckill.dao.GoodsDao;
import com.lijs.seckill.domain.SeckillGoods;
import com.lijs.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    private GoodsDao goodsDao;

    public List<GoodsVo> getGoodsVoList() {
        return goodsDao.getGoodsVoList();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    public void reduceStock(GoodsVo goodsvo) {
        SeckillGoods goods = new SeckillGoods();
        goods.setGoodsId(goodsvo.getId());
        goodsDao.reduceStock(goods);
    }

    /**
     * 考虑有可能下单失败的情况,下单失败那么就不去
     */
    public boolean reduceStockRes(GoodsVo goodsvo) {
        SeckillGoods goods = new SeckillGoods();
        goods.setGoodsId(goodsvo.getId());
        int ret = goodsDao.reduceStock(goods);
        System.out.println("reduceStock1:" + ret);
        return ret > 0;
    }
}
