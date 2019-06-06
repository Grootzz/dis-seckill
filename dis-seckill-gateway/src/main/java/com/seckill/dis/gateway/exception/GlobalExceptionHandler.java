package com.seckill.dis.gateway.exception;

import com.seckill.dis.common.result.CodeMsg;
import com.seckill.dis.common.result.Result;
import com.seckill.dis.gateway.user.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 全局异常处理器（底层使用方法拦截的方式完成，和AOP一样）
 * 在异常发生时，将会调用这里面的方法给客户端一个响应
 *
 * @author noodle
 */

@ControllerAdvice // 通过Advice可知，这个处理器实际上是一个切面
@ResponseBody
public class GlobalExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 异常处理
     *
     * @param request 绑定了出现异常的请求信息
     * @param e       该请求所产生的异常
     * @return 向客户端返回的结果（这里为json数据）
     */
    @ExceptionHandler(value = Exception.class)// 这个注解用指定这个方法对何种异常处理（这里默认所有异常都用这个方法处理）
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e) {
        logger.info("出现异常");
        e.printStackTrace();// 打印原始的异常信息，方便调试

        // 如果所拦截的异常是自定义的全局异常，这按自定义异常的处理方式处理，否则按默认方式处理
        if (e instanceof GlobalException) {
            GlobalException exception = (GlobalException) e;
            // 向客户端返回异常信息
            return Result.error(exception.getCodeMsg());
        } else if (e instanceof BindException) {
            BindException bindException = (BindException) e;
            List<ObjectError> errors = bindException.getAllErrors();
            // 这里只获取了第一个错误对象
            ObjectError error = errors.get(0);
            // 获取其中的信息
            String message = error.getDefaultMessage();
            // 将错误信息动态地拼接到已定义的部分信息上
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(message));
        } else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
