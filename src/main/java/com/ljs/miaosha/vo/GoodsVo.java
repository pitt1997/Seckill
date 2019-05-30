package com.ljs.miaosha.vo;

import java.util.Date;

import com.ljs.miaosha.domain.Goods;
//将Goods表和MiaoshaGoods表合并
public class GoodsVo extends Goods{
	private Integer stockCount;
	private Date startDate;
	private Date endDate;
	private Double miaoshaPrice;
	
	
	public Double getMiaoshaPrice() {
		return miaoshaPrice;
	}
	public void setMiaoshaPrice(Double miaoshaPrice) {
		this.miaoshaPrice = miaoshaPrice;
	}
	public Integer getStockCount() {
		return stockCount;
	}
	public void setStockCount(Integer stockCount) {
		this.stockCount = stockCount;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
}
