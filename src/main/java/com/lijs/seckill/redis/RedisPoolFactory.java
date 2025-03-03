package com.lijs.seckill.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisPoolFactory {

    @Autowired
    private RedisConfig redisConfig;

    /**
     * JedisPool的实例注入到spring容器里面
     */
    @Bean
    public JedisPool JedisPoolFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        System.out.println("redisConfig.getPoolMaxldle():" + redisConfig.getPoolMaxldle());
        System.out.println("redisConfig.getPoolMaxTotal():" + redisConfig.getPoolMaxTotal());
        System.out.println("redisConfig.getPoolMaxWait():" + redisConfig.getPoolMaxWait());
        System.out.println("redisConfig.getPassword():" + redisConfig.getPassword());
        poolConfig.setMaxIdle(redisConfig.getPoolMaxldle());
        poolConfig.setMaxTotal(redisConfig.getPoolMaxTotal());
        poolConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait() * 1000);
        return new JedisPool(poolConfig, redisConfig.getHost(), redisConfig.getPort(),
                redisConfig.getTimeout() * 1000, redisConfig.getPassword(), 0);
    }
}
