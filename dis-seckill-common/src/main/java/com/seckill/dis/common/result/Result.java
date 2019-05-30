package com.seckill.dis.common.result;

import java.io.Serializable;

/**
 * 用户接口返回结果
 *
 * @author noodle
 */
public class Result<T> implements Serializable{

    private int code;
    private String msg;
    private T data;

    /**
     * 定义为private是为了在防止在controller中直接new
     *
     * @param data
     */
    private Result(T data) {
        this.code = 0;
        this.msg = "success";
        this.data = data;
    }

    private Result(CodeMsg serverError) {
        if (serverError == null)
            return;
        this.code = serverError.getCode();
        this.msg = serverError.getMsg();
    }

    /**
     * 只有get没有set，是为了防止在controller使用set对结果修改，从而达到一个更好的封装效果
     *
     * @return
     */
    public int getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    /**
     * 成功时候的调用返回结果
     *
     * @param data
     * @return
     */
    public static <T> Result<T> success(T data) {
        return new Result<T>(data);
    }


    public static <T> Result<T> error(CodeMsg serverError) {
        return new Result<T>(serverError);
    }
}
