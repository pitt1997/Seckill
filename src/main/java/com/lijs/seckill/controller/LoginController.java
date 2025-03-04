package com.lijs.seckill.controller;

import ch.qos.logback.classic.Logger;
import com.lijs.seckill.result.ResultCode;
import com.lijs.seckill.result.Result;
import com.lijs.seckill.service.SeckillUserService;
import com.lijs.seckill.vo.LoginVo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequestMapping("/login")
@Controller
public class LoginController {

    @Autowired
    private SeckillUserService seckillUserService;

    private final Logger logger = (Logger) LoggerFactory.getLogger(Logger.class);

    @RequestMapping("/index")
    public String toLogin() {
        // 返回页面login
        logger.info("跳转登录首页...");
        return "login";
    }

    @RequestMapping("/token_test")
    @ResponseBody
    public Result<String> doLoginTest(HttpServletResponse response, @Valid LoginVo loginVo) {
        String token = seckillUserService.loginTest(response, loginVo);
        return Result.success(token);
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        ResultCode resultCode = seckillUserService.login(response, loginVo);
        if (resultCode.getCode() == 0) {
            return Result.success(true);
        } else {
            return Result.error(resultCode);
        }
    }
}
