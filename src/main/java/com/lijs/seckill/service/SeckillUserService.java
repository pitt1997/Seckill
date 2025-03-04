package com.lijs.seckill.service;

import com.lijs.seckill.dao.SeckillUserDao;
import com.lijs.seckill.domain.SeckillUser;
import com.lijs.seckill.redis.RedisService;
import com.lijs.seckill.redis.SeckillUserKey;
import com.lijs.seckill.result.ResultCode;
import com.lijs.seckill.util.MD5Util;
import com.lijs.seckill.util.UUIDUtil;
import com.lijs.seckill.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class SeckillUserService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private SeckillUserDao seckillUserDao;

    @Autowired
    private RedisService redisService;

    /**
     * 根据id取得对象，先去缓存中取
     */
    public SeckillUser getById(long id) {
        // 1.先根据id尝试查询缓存
        SeckillUser user = redisService.get(SeckillUserKey.getById, "" + id, SeckillUser.class);
        // 缓存中拿到
        if (user != null) {
            return user;
        }
        // 2.缓存中拿不到，那么就去取数据库
        user = seckillUserDao.getById(id);
        if (user != null) {
            // 3.数据库存在则设置缓存
            redisService.set(SeckillUserKey.getById, "" + id, user);
        }
        return user;
    }

    /**
     * 根据token获取用户信息
     */
    public SeckillUser getByToken(String token, HttpServletResponse response) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }

        SeckillUser user = redisService.get(SeckillUserKey.token, token, SeckillUser.class);
        // 再次请求的时候，延长有效期
        if (user != null) {
            addCookie(user, token, response);
        }
        return user;
    }

    public String loginTest(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            return ResultCode.SERVER_ERROR.getMsg();
        }
        // 经过一次MD5的密码
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        // 判断手机号是否存在
        SeckillUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            // 查询不到该手机号的用户
            return ResultCode.MOBILE_NOT_EXIST.getMsg();
        }
        // 手机号存在则验证密码，获取数据库里面的密码与salt去验证
        // eg. 111111 -> e5d22cfc746c7da8da84e0a996e0fffa
        String dbPass = user.getPwd();
        String dbSalt = user.getSalt();
        logger.info("dbPass:{}   dbSalt:{}", dbPass, dbSalt);
        // 验证密码，计算二次MD5出来的pass是否与数据库一致
        String tmpPass = MD5Util.formPassToDBPass(password, dbSalt);
        logger.info("formPass:{}   tmpPass:{}", password, tmpPass);
        if (!tmpPass.equals(dbPass)) {
            return ResultCode.PASSWORD_ERROR.getMsg();
        }
        // 生成cookie
        String token = UUIDUtil.uuid();
        addCookie(user, token, response);
        return token;
    }

    public ResultCode login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            return ResultCode.SERVER_ERROR;
        }
        // 经过一次MD5的密码
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        // 判断手机号是否存在
        SeckillUser user = getById(Long.parseLong(mobile));
        // 查询不到该手机号的用户
        if (user == null) {
            return ResultCode.MOBILE_NOT_EXIST;
        }
        // 手机号存在的情况，验证密码，获取数据库里面的密码与salt去验证
        // eg. 111111--->e5d22cfc746c7da8da84e0a996e0fffa
        String dbPass = user.getPwd();
        String dbSalt = user.getSalt();
        logger.info("dbPass:{}, dbSalt:{}", dbPass, dbSalt);
        // 验证密码，计算二次MD5出来的pass是否与数据库一致
        String tmpPass = MD5Util.formPassToDBPass(formPass, dbSalt);
        logger.info("formPass:{}, tmpPass:{}", formPass, tmpPass);
        if (!tmpPass.equals(dbPass)) {
            return ResultCode.PASSWORD_ERROR;
        }
        // 生成cookie
        String token = UUIDUtil.uuid();
        addCookie(user, token, response);
        return ResultCode.SUCCESS;

    }

    /**
     * 添加或者更新cookie
     */
    public void addCookie(SeckillUser user, String token, HttpServletResponse response) {
        // 可以用旧的token，不用每次都生成cookie
        // 将token写到cookie当中，然后传递给客户端
        // 此token对应的是哪一个用户,将我们的私人信息存放到一个第三方的缓存中
        // prefix:SeckillUserKey.token key:token value:用户的信息 -->以后拿到了token就知道对应的用户信息。
        // SeckillUserKey.token-->
        redisService.set(SeckillUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        // 设置cookie的有效期，与session有效期一致
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        // 设置网站的根目录
        cookie.setPath("/");
        // 需要写到response中
        response.addCookie(cookie);
    }
}
