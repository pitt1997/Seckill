package com.ljs.miaosha.vo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.ljs.miaosha.util.IsMobile;

public class LoginVo {
	private String mobile;
	private String password;
	
	@NotNull
	@IsMobile
	public String getMobile() {
		return mobile;
	}
	
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	@NotNull
	@Length(min=32)
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
