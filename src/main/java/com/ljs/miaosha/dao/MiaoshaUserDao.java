package com.ljs.miaosha.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.ljs.miaosha.domain.MiaoshaUser;

@Mapper
public interface MiaoshaUserDao {
	@Select("select * from miaosha_user where id=#{id}")  //这里#{id}通过后面参数来为其赋值
	public MiaoshaUser getById(@Param("id") long id);    //绑定
	
	//绑定在对象上面了----@Param("id")long id,@Param("pwd")long pwd 效果一致
	@Update("update miaosha_user set pwd=#{pwd} where id=#{id}")
	public void update(MiaoshaUser toupdateuser);
	
	//public boolean update(@Param("id")long id);    //绑定
	
}
