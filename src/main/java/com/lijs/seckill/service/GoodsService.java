package com.lijs.seckill.service;

import com.lijs.seckill.dao.GoodsDao;
import com.lijs.seckill.domain.SeckillGoods;
import com.lijs.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    private final Logger logger = LoggerFactory.getLogger(GoodsService.class);

    @Autowired
    private GoodsDao goodsDao;

    public List<GoodsVo> getGoodsVoList() {
        return goodsDao.getGoodsVoList();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    /**
     * 考虑减少库存失败的时候，不进行写入订单
     */
    public boolean reduceStock(GoodsVo goodsVo) {
        SeckillGoods goods = new SeckillGoods();
        goods.setGoodsId(goodsVo.getId());
        int ret = goodsDao.reduceStock(goods);
        logger.info("reduceStock result: {}", ret);
        return ret > 0;
    }
}
