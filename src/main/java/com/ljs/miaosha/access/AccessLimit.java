package com.ljs.miaosha.access;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
public @interface AccessLimit {
	int seconds();
	int maxCount();
	boolean needLogin() default true;
}
