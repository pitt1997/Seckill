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
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    // 拦截所有的异常
    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e) {
        if (e instanceof GlobalException) {
            GlobalException ex = (GlobalException) e;
            ResultCode cm = ex.getCm();
            return Result.error(cm);
        }
        // 是绑定异常的情况
        if (e instanceof BindException) {
            BindException ex = (BindException) e;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();
            return Result.error(ResultCode.BIND_ERROR.fillArgs(msg));
        } else {
            // 不是绑定异常的情况
            return Result.error(ResultCode.SERVER_ERROR);
        }
    }
}
