package com.lijs.seckill.dao;

import com.lijs.seckill.domain.OrderInfo;
import com.lijs.seckill.domain.SeckillOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {
    @Select("select * from seckill_order where user_id=#{userId} and goods_id=#{goodsId}")
    SeckillOrder getSeckillOrderByUserIdAndGoodsId(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    @Insert("insert into order_info (user_id,goods_id,goods_name,goods_count,goods_price,order_channel,order_status,create_date) values "
            + "(#{userId},#{goodsId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{orderStatus},#{createDate})")
    @SelectKey(keyColumn = "id", keyProperty = "id", resultType = long.class, before = false, statement = "select last_insert_id()")
    long insert(OrderInfo orderInfo);

    @Select("select * from order_info where user_id=#{userId} and goods_id=#{goodsId}")
    OrderInfo selectorderInfo(@Param("userId") Long userId, @Param("goodsId") Long goodsId);//使用注解获取返回值，返回上一次插入的id

    @Insert("insert into seckill_order (user_id,goods_id,order_id) values (#{userId},#{goodsId},#{orderId})")
    void insertSeckillOrder(SeckillOrder seckillOrder);

    @Select("select * from order_info where id=#{orderId}")
    OrderInfo getOrderByOrderId(@Param("orderId") long orderId);

}
