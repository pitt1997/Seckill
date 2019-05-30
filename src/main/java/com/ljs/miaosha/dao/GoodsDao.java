package com.ljs.miaosha.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.ljs.miaosha.domain.MiaoshaGoods;
import com.ljs.miaosha.vo.GoodsVo;

@Mapper
public interface GoodsDao {
	//两个查询
	@Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id=g.id")  
	public List<GoodsVo> getGoodsVoList();
	@Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id=g.id where g.id=#{goodsId}")  
	public GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);
	//stock_count>0的时候才去更新，数据库本身会有锁，那么就不会在数据库中同时多个线程更新一条记录，使用数据库特性来保证超卖的问题
	@Update("update miaosha_goods set stock_count=stock_count-1 where goods_id=#{goodsId} and stock_count>0")
	public void reduceStock(MiaoshaGoods goods);    
	
	@Update("update miaosha_goods set stock_count=stock_count-1 where goods_id=#{goodsId} and stock_count>0")
	public int reduceStock1(MiaoshaGoods goods);    
//	@Update("update miaosha_goods set stock_count=stock_count-1 where goods_id=#{goodsId}")
//	public void reduceStock(@Param("goodsId") long goodsId);   
}
