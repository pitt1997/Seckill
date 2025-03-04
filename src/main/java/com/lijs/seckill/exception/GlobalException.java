package com.lijs.seckill.exception;


import com.lijs.seckill.result.ResultCode;

/**
 * 全局异常处理
 */
public class GlobalException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private ResultCode cm;

    public GlobalException(ResultCode cm) {
        super(cm.toString());
        this.cm = cm;

    }

    public ResultCode getCm() {
        return cm;
    }

    public void setCm(ResultCode cm) {
        this.cm = cm;
    }

}
