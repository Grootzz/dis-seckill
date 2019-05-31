package com.seckill.dis.common.result;

import java.io.Serializable;

/**
 * 用户接口返回结果
 *
 * @param <T> 数据实体类型
 * @author noodle
 */
public class Result<T> implements Serializable {

    /**
     * 状态码
     */
    private int code;

    /**
     * 状态短语
     */
    private String msg;

    /**
     * 数据实体
     */
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

    private Result(CodeMsg codeMsg) {
        if (codeMsg == null)
            return;
        this.code = codeMsg.getCode();
        this.msg = codeMsg.getMsg();
    }

    /**
     * 只有get没有set，是为了防止在controller使用set对结果修改，从而达到一个更好的封装效果
     *
     * @return
     */
    public int getCode() {
        return code;
    }

    /**
     * 业务处理成功返回结果，直接返回业务数据
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

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
