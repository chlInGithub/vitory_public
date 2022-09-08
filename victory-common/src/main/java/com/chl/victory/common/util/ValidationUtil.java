package com.chl.victory.common.util;

import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Set;

/**
 *  validation工具类
 *  参考: http://hibernate.org/validator/documentation/getting-started/
 *  依赖javax.validation:validation-api,org.springframework.boot:spring-boot-starter-validation
 * @author hailongchen9
 */
public class ValidationUtil {
    final static Validator validator = Validation.byProvider(HibernateValidator .class)
            .configure().failFast(true).buildValidatorFactory()
            .getValidator();

    /**
     * validate行为，验证失败会抛出ValidationException。
     * @param model 需要validate的对象
     */
    public static <T> void validate(T model){
        Set<ConstraintViolation<T>> result = validator.validate(model);
        if (result.isEmpty()){
            return;
        }

        ConstraintViolation constraintViolation = result.iterator().next();
        String msg = constraintViolation.getRootBeanClass().getSimpleName() + "#" + constraintViolation.getPropertyPath() + "|" + constraintViolation.getMessage();
        throw new ValidationException(msg);
    }
}
