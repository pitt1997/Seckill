package com.lijs.seckill.service;

import com.lijs.seckill.dao.UserDao;
import com.lijs.seckill.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public User getById(int id) {
        return userDao.getById(id);
    }

    /**
     * 使用事务
     */
    @Transactional
    public boolean tx() {
        User user1 = new User();
        user1.setId(3);
        user1.setName("lijs1");
        userDao.insert(user1);

        User user2 = new User();
        user2.setId(1);
        user2.setName("lijs2");
        userDao.insert(user2);

        return true;
    }
}
