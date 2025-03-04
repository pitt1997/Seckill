package com.lijs.seckill.access;

import com.alibaba.fastjson.JSON;
import com.lijs.seckill.domain.SeckillUser;
import com.lijs.seckill.redis.AccessKey;
import com.lijs.seckill.redis.RedisService;
import com.lijs.seckill.result.Result;
import com.lijs.seckill.result.ResultCode;
import com.lijs.seckill.service.SeckillUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    private final Logger logger = LoggerFactory.getLogger(AccessInterceptor.class);

    @Autowired
    private SeckillUserService seckillUserService;
    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            // 根据请求中的token获取会话用户信息
            SeckillUser user = getUser(request, response);
            logger.info("用户信息:{}", user);
            // 将user保存到会话变量中
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            // 没有AccessLimit注解时则不进行拦截操作
            if (accessLimit == null) {
                return true;
            }
            // 获取 AccessLimit 参数
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            logger.info("key:{}", key);
            if (needLogin) {
                if (user == null) {
                    // 需要给客户端一个提示session失效
                    render(response, ResultCode.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            }
            // 限定key 5s 之内只能访问 5次，动态设置有效期
            AccessKey accessKey = AccessKey.expire(seconds);
            Integer count = redisService.get(accessKey, key, Integer.class);
            if (count == null) {
                redisService.set(accessKey, key, 1);
            } else if (count < maxCount) {
                redisService.incr(accessKey, key);
            } else {
                // 超过5次则将结果给前端
                render(response, ResultCode.ACCESS_LIMIT);
                return false;
            }
        }
        return super.preHandle(request, response, handler);
    }

    private void render(HttpServletResponse response, ResultCode cm) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String jsonString = JSON.toJSONString(Result.error(cm));
        out.write(jsonString.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private SeckillUser getUser(HttpServletRequest request, HttpServletResponse response) {
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
        return seckillUserService.getByToken(token, response);
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
}
