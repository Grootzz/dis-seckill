package com.seckill.dis.common.validator;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 手机号码的校验注解
 *
 * @author
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsMobileValidator.class}) // 这个注解的参数指定用于校验工作的是哪个类
public @interface IsMobile {

    // 默认手机号码不可为空
    boolean required() default true;

    // 如果校验不通过时的提示信息
    String message() default "手机号码格式有误！";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
