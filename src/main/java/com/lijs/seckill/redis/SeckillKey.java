package com.lijs.seckill.redis;

public class SeckillKey extends BasePrefix {

    public SeckillKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillKey isGoodsOver = new SeckillKey(0, "go");
    /**
     * 有效期60s
     */
    public static SeckillKey getSeckillPath = new SeckillKey(60, "mp");
    /**
     * 验证码 有效期300s
     */
    public static SeckillKey getSeckillVerifyCode = new SeckillKey(300, "vc");
}
