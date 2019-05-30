package com.ljs.miaosha.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//作用 声明当前类是一个配置类,相当于一个Spring的XML配置文件,与@Bean配
//@Configuration标注在类上，相当于把该类作为spring的xml配置文件中的<beans>，作用为：配置spring容器(应用上下文)
@Configuration
public class MQConfig {
	public static final String QUEUE="queue";
	public static final String MIAOSHA_QUEUE="miaosha.queue";
	
	public static final String TOPIC_QUEUE1="topic.queue1";
	public static final String TOPIC_QUEUE2="topic.queue2";
	public static final String HEADER_QUEUE="header.queue";
	public static final String TOPIC_EXCHANGE="topic.exchange";
	public static final String FANOUT_EXCHANGE="fanout.exchange";
	public static final String HEADER_EXCHANGE="header.exchange";
	public static final String ROUTINIG_KEY1="topic.key1";
	public static final String ROUTINIG_KEY2="topic.#";
	/**
	 * Direct模式，交换机Exchange:
	 * 发送者，将消息往外面发送的时候，并不是直接投递到队列里面去，而是先发送到交换机上面，然后由交换机发送数据到queue上面去，
	 * 做了依次路由。
	 */
	@Bean
	public Queue queue() {
		//名称，是否持久化
		return new Queue(QUEUE,true);
	}
	
	@Bean
	public Queue miaoshaqueue() {
		//名称，是否持久化
		return new Queue(MIAOSHA_QUEUE,true);
	}
	/**
	 * Topic模式 交换机Exchange：
	 * 
	 */
//	@Bean
//	public Queue topicqueue1() {
//		//名称，是否持久化
//		return new Queue(TOPIC_QUEUE1,true);
//	}
//	/**
//	 * Topic模式 交换机：
//	 * 
//	 */
//	@Bean
//	public Queue topicqueue2() {
//		//名称，是否持久化
//		return new Queue(TOPIC_QUEUE2,true);
//	}
//	
//	/**
//	 * 先将消息放到exchange中去，再把消息放到queue里面去。
//	 * @return
//	 */
//	@Bean
//	public TopicExchange topicExchange() {
//		//名称，
//		return new TopicExchange(TOPIC_EXCHANGE);
//	}
//	
//	
//	@Bean
//	public Binding topicBinding1() {
//		//绑定到某一个queue上面
//		return BindingBuilder.bind(topicqueue1()).to(topicExchange()).with(ROUTINIG_KEY1);//"topic.key1"
//	}
//	@Bean
//	public Binding topicBinding2() {
//		//绑定到某一个queue上面
//		return BindingBuilder.bind(topicqueue2()).to(topicExchange()).with(ROUTINIG_KEY2);//"topic.#"  #匹配一个或者多个
//	}
//	/**
//	 * Fanout模式 交换机Exchange
//	 * 广播模式
//	 */
//	@Bean
//	public FanoutExchange fanoutExchange() {
//		//名称，
//		return new FanoutExchange(FANOUT_EXCHANGE);
//	}
//	@Bean
//	public Binding fanoutBinding1() {
//		//绑定到某一个queue上面
//		return BindingBuilder.bind(topicqueue1()).to(fanoutExchange());//广播，没有key的
//	}
//	
//	@Bean
//	public Binding fanoutBinding2() {
//		//绑定到某一个queue上面
//		return BindingBuilder.bind(topicqueue2()).to(fanoutExchange());//广播，没有key的
//	}
//	
//	
//	/**
//	 * HEADER
//	 * header模式 交换机Exchange
//	 * 
//	 * @return
//	 */
//	@Bean
//	public HeadersExchange headersExchange() {
//		// 名称，
//		return new HeadersExchange(HEADER_EXCHANGE);
//	}
//	/**
//	 * 新建一个headerqueue
//	 * @return
//	 */
//	@Bean
//	public Queue headerQueue1() {
//		//名称，是否持久化
//		return new Queue(HEADER_QUEUE,true);
//	}
//	/**
//	 * headerbinding
//	 * 
//	 * 在这个message里面，会有一个head部分，必须满足map里面的key-value，然后符合了之后
//	 * 才能在queue里面放东西。
//	 */
//	@Bean
//	public Binding headerBinding() {
//		//绑定到某一个queue上面
//		Map<String,Object> map=new HashMap<String,Object>();
//		map.put("header1","value1" );
//		map.put("header2","value2" );
//		return BindingBuilder.bind(headerQueue1()).to(headersExchange()).whereAll(map).match();
//	}
}
