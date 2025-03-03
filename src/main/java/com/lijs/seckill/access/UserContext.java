package com.lijs.seckill.access;

import com.lijs.seckill.domain.SeckillUser;

public class UserContext {

    private static final ThreadLocal<SeckillUser> userHolder = new ThreadLocal<SeckillUser>();

    public static void setUser(SeckillUser user) {
        userHolder.set(user);
    }

    public static SeckillUser getUser() {
        return userHolder.get();
    }
}
