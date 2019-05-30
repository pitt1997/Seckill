package com.ljs.miaosha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

//@EnableAutoConfiguration
//一般@SpringBootApplication
/**
 * war包
 * @author 17996
 *
 */
//@SpringBootApplication
//public class MainApplication extends SpringBootServletInitializer{
//	public static void main(String[] args) {
//		SpringApplication.run(MainApplication.class, args);
//	}
//	
//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//		//return super.configure(builder);
//		return builder.sources(MainApplication.class);
//	}
//}
/**
 * jar包
 */
@SpringBootApplication
public class MainApplication {
	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}
}