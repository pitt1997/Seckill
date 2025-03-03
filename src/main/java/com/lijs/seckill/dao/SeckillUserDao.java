package com.lijs.seckill.dao;

import com.lijs.seckill.domain.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillUserDao {
    @Select("select * from seckill_user where id=#{id}")
    SeckillUser getById(@Param("id") long id);

    @Update("update seckill_user set pwd=#{pwd} where id=#{id}")
    void update(SeckillUser updateUser);

}
