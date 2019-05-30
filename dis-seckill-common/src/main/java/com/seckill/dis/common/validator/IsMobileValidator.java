package com.seckill.dis.common.validator;

import com.seckill.dis.common.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 真正用户手机号码检验的工具，会被注解@isMobile所使用
 * 这个类需要实现javax.validation.ConstraintValidator，否则不能被@Constraint参数使用
 *
 * @author noodle
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    private static Logger logger = LoggerFactory.getLogger(IsMobileValidator.class);

    /**
     * 用于获取检验字段是否可以为空
     */
    private boolean required = false;

    /**
     * 用于获取注解
     *
     * @param constraintAnnotation
     */
    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    /**
     * 用于检验字段是否合法
     *
     * @param value   待校验的字段
     * @param context
     * @return 字段检验结果
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        logger.info("是否需要校验参数："+required);
        // 如果所检验字段可以为空
        if (required) {
            logger.info(""+ValidatorUtil.isMobile(value));
            return ValidatorUtil.isMobile(value);// 检验结果
        } else {
            if (StringUtils.isEmpty(value))
                return true;
            else
                return ValidatorUtil.isMobile(value);// 校验结果
        }
    }
}
