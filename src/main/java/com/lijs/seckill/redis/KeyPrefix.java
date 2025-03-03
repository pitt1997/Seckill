package com.lijs.seckill.redis;

/**
 * 做缓存的前缀接口
 */
public interface KeyPrefix {

    /**
     * 有效期
     */
    int expireSeconds();

    /**
     * 前缀
     */
    String getPrefix();

}
