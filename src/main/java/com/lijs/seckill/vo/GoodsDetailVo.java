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
    private int remainingSeconds = 0;
    private GoodsVo goodsVo;
    private SeckillUser user;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getremainingSeconds() {
        return remainingSeconds;
    }

    public void setremainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
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
