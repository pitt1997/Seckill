package com.lijs.seckill.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    public static final String QUEUE = "queue";
    public static final String SECKILL = "seckill.queue";

    /**
     * Direct模式，交换机Exchange:
     * 发送者，将消息往外面发送的时候，并不是直接投递到队列里面去，而是先发送到交换机上面，然后由交换机发送数据到queue上面去，
     * 做了依次路由。
     */
    @Bean
    public Queue queue() {
        // 参数：队列名称，是否持久化（true）
        return new Queue(QUEUE, true);
    }
}
