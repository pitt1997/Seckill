package com.ljs.miaosha.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.ljs.miaosha.access.AccessInterceptor;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter{
	@Autowired
	UserArgumentResolver userArgumentResolver;
	@Autowired
	AccessInterceptor accessInterceptor;
	
	/**
	 * 设置一个MiaoshaUser参数给，toList使用
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		//将UserArgumentResolver注册到config里面去	
		argumentResolvers.add(userArgumentResolver);
	}
	
	/**
	 * 注册拦截器
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		//注册
		//registry.addInterceptor(accessInterceptor);
		super.addInterceptors(registry);
	}
	
}
