package com.lijs.seckill.vo;

import com.lijs.seckill.domain.SeckillUser;

/**
 * 为了给页面传值
 *
 * @author 17996
 */
public class GoodsDetailVo {
    // 秒杀状态量
    private int status = 0;
    // 开始时间倒计时
    private int remailSeconds = 0;
    private GoodsVo goodsVo;
    private SeckillUser user;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRemailSeconds() {
        return remailSeconds;
    }

    public void setRemailSeconds(int remailSeconds) {
        this.remailSeconds = remailSeconds;
    }

    public GoodsVo getGoodsVo() {
        return goodsVo;
    }

    public void setGoodsVo(GoodsVo goodsVo) {
        this.goodsVo = goodsVo;
    }

    public SeckillUser getUser() {
        return user;
    }

    public void setUser(SeckillUser user) {
        this.user = user;
    }


}
