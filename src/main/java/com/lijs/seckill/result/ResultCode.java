package com.lijs.seckill.result;

/**
 * 成功的时候，只返回数据。
 * 失败的话，那么就返回错误码以及错误信息
 */
public class ResultCode {

    private int code;

    private String msg;

    public static ResultCode SUCCESS = new ResultCode(0, "success");
    public static ResultCode SERVER_ERROR = new ResultCode(500100, "服务端异常!");
    public static ResultCode BIND_ERROR = new ResultCode(500101, "参数校验异常:%s");
    public static ResultCode REQUEST_ILLEGAL = new ResultCode(500102, "非法请求!");
    public static ResultCode SECKILL_FAIL = new ResultCode(500103, "秒杀失败!");
    public static ResultCode ACCESS_LIMIT = new ResultCode(500104, "达到访问限制次数，访问太频繁!");
    public static ResultCode SESSION_ERROR = new ResultCode(500210, "session失效!");
    public static ResultCode PASSWORD_EMPTY = new ResultCode(500211, "密码不能为空!");
    public static ResultCode MOBILE_EMPTY = new ResultCode(500212, "手机号不能为空!");
    public static ResultCode MOBILE_ERROR = new ResultCode(500213, "手机号格式错误!");
    public static ResultCode MOBILE_NOT_EXIST = new ResultCode(500214, "手机号号码不存在!");
    public static ResultCode PASSWORD_ERROR = new ResultCode(500215, "密码错误!");
    public static ResultCode ORDER_NOT_EXIST = new ResultCode(500410, "订单不存在!");
    public static ResultCode SECKILL_OVER_ERROR = new ResultCode(500500, "商品秒杀完毕，库存不足!");
    public static ResultCode REPEAT_SECKILL = new ResultCode(500500, "不能重复秒杀!");

    public ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ResultCode fillArgs(Object... args) {//变参
        int code = this.code;
        String message = String.format(this.msg, args);
        return new ResultCode(code, message);
    }

    @Override
    public String toString() {
        return "Result [code=" + code + ", msg=" + msg + "]";
    }

}
