package com.lijs.seckill.exception;

import com.lijs.seckill.result.ResultCode;
import com.lijs.seckill.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 相当于一个controller
 * @author 17996
 *
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
	//拦截什么异常
	@ExceptionHandler(value=Exception.class)//拦截所有的异常
	public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
		e.printStackTrace();
		if(e instanceof GlobalException) {
			GlobalException ex=(GlobalException) e;
			ResultCode cm=ex.getCm();
			return Result.error(cm);
		}
		if(e instanceof BindException) {//是绑定异常的情况
			//强转
			BindException ex=(BindException) e;
			//获取错误信息
			List<ObjectError> errors=ex.getAllErrors();
			ObjectError error=errors.get(0);
			String msg=error.getDefaultMessage();
			return Result.error(ResultCode.BIND_ERROR.fillArgs(msg));
		}else {//不是绑定异常的情况
			return Result.error(ResultCode.SERVER_ERROR);
		}
	}
}
