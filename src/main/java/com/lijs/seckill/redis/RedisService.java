package com.lijs.seckill.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@DependsOn("jedisPool") // 确保 jedisPoolConfig 先加载
@Service
public class RedisService {

    private final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private JedisPool jedisPool;

    /**
     * 获取单个对象
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> data) {
        logger.info("get key:{}", key);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 生成真正的key  className+":"+prefix;  BasePrefix:id1
            String realKey = prefix.getPrefix() + key;
            logger.info("get realKey:{}", realKey);
            String value = jedis.get(realKey);
            logger.info("get value:{}", value);
            // 将String转换为Bean
            return stringToBean(value, data);
        } finally {
            close(jedis);
        }
    }

    /**
     * redis删除对象
     */
    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            long ret = jedis.del(realKey);
            // 删除成功，返回大于0
            return ret > 0;
        } finally {
            close(jedis);
        }
    }

    /**
     * 设置redis对象
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        logger.info("set key:{}", key);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            logger.info("set realKey:{}", realKey);
            String s = beanToString(value);
            if (s == null || s.isEmpty()) {
                return false;
            }
            int seconds = prefix.expireSeconds();
            if (seconds <= 0) {
                // 有效期：小于0代表不过期
                jedis.set(realKey, s);
            } else {
                // 设置过期时间
                jedis.setex(realKey, seconds, s);
            }
            return true;
        } finally {
            close(jedis);
        }
    }

    /**
     * 减少值
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.decr(realKey);
        } finally {
            close(jedis);
        }
    }

    /**
     * 增加值
     */
    public <T> void incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            jedis.incr(realKey);
        } finally {
            close(jedis);
        }
    }

    /**
     * 检查key是否存在
     */
    public <T> boolean exitsKey(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.exists(realKey);
        } finally {
            close(jedis);
        }
    }

    /**
     * 将字符串转换为Bean对象
     * <p>
     * parseInt()返回的是基本类型int 而valueOf()返回的是包装类Integer
     * Integer是可以使用对象方法的  而int类型就不能和Object类型进行互相转换 。
     * int a=Integer.parseInt(s);
     * Integer b=Integer.valueOf(s);
     */
    public static <T> T stringToBean(String s, Class<T> clazz) {
        if (s == null || s.isEmpty() || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return ((T) Integer.valueOf(s));
        } else if (clazz == String.class) {
            return (T) s;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(s);
        } else {
            JSONObject json = JSON.parseObject(s);
            return JSON.toJavaObject(json, clazz);
        }
    }

    /**
     * 将Bean对象转换为字符串类型
     */
    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == Integer.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return "" + value;
        } else if (clazz == Long.class) {
            return "" + value;
        } else {
            return JSON.toJSONString(value);
        }
    }

    /**
     * 关闭连接
     */
    private void close(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public <T> boolean set(String key, T value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 将T类型转换为String类型
            String s = beanToString(value);
            if (s == null) {
                return false;
            }
            jedis.set(key, s);
            return true;
        } finally {
            close(jedis);
        }
    }

    public <T> T get(String key, Class<T> data) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String value = jedis.get(key);
            return stringToBean(value, data);
        } finally {
            close(jedis);
        }
    }

    /**
     * 预减库存（秒杀库存扣减）
     * @param prefix key前缀
     * @param key 商品ID对应的库存Key
     * @return 扣减成功返回true，库存不足返回false
     */
    public boolean preDecrStock(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;

            // 先检查库存是否存在
            if (!jedis.exists(realKey)) {
                logger.warn("库存key不存在: {}", realKey);
                return false;
            }

            // 预减库存
            long stock = jedis.decr(realKey);
            logger.info("库存预减后数量: {}, key: {}", stock, realKey);

            if (stock < 0) {
                // 如果库存小于 0，则回滚库存
                jedis.incr(realKey);
                logger.warn("库存不足，回滚库存, key: {}", realKey);
                return false;
            }
            return true;
        } finally {
            close(jedis);
        }
    }

    /**
     * 回滚库存（如果秒杀失败）
     * @param prefix key前缀
     * @param key 商品ID对应的库存Key
     */
    public void rollbackStock(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            jedis.incr(realKey);
            logger.info("库存回滚成功, key: {}", realKey);
        } finally {
            close(jedis);
        }
    }
}
