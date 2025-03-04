package com.lijs.seckill.config;

import com.lijs.seckill.domain.SeckillUser;
import com.lijs.seckill.service.SeckillUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private final Logger logger = LoggerFactory.getLogger(UserArgumentResolver.class);

    @Autowired
    private SeckillUserService seckillUserService;

    public Object resolveArgument(MethodParameter arg0, ModelAndViewContainer arg1, NativeWebRequest webRequest,
                                  WebDataBinderFactory arg3) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        // 通过支持解析参数或者请求头中的Token来支撑分布式Session（Session存储在redis，不同的请求只要带上token就可以进行会话）
        String paramToken = request.getParameter(SeckillUserService.COOKIE_NAME_TOKEN);
        String headerToken = request.getHeader("Authorization");
        logger.info("paramToken:{}", paramToken);
        // 获取cookie
        String cookieToken = getCookieValue(request, SeckillUserService.COOKIE_NAME_TOKEN);
        logger.info("cookieToken:{}", cookieToken);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken) && StringUtils.isEmpty(headerToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(headerToken) ? (StringUtils.isEmpty(paramToken) ? cookieToken : paramToken) : headerToken;
        SeckillUser user = seckillUserService.getByToken(token, response);
        logger.info("user:{}", user);
        return user;
    }

    public String getCookieValue(HttpServletRequest request, String cookieNameToken) {
        // 遍历request里面所有的cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieNameToken)) {
                    logger.info("cookie:{}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        logger.info("cookie not find.");
        return null;
    }

    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz == SeckillUser.class;
    }
}
