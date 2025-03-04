package com.lijs.seckill.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class ValidatorUtil {

	/**
	 * 手机号正则匹配
	 */
	public static final String REG_MOBILE_TELEPHONE = "^(1[3-9])\\d{9}$";
	public static final Pattern MOBILE_TELEPHONE_PATTERN = Pattern.compile(REG_MOBILE_TELEPHONE);

	/**
	 * 验证手机号格式
	 */
	public static boolean isMobile(String src) {
        if (StringUtils.isEmpty(src)) {
            return false;
        }
        Matcher m = MOBILE_TELEPHONE_PATTERN.matcher(src);
        return m.matches();
    }
}
