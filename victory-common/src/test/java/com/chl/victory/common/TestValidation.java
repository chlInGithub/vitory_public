package com.chl.victory.common;

import com.chl.victory.common.util.ValidationUtil;
import lombok.NonNull;
import org.hibernate.validator.HibernateValidator;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.Set;

/**
 * @author ChenHailong
 * @date 2019/5/10 14:05
 **/
public class TestValidation{
    @Test
    public void testValid(){
        t2(new Page());
    }

   <T> void t2  (T page){
       ValidationUtil.validate(page);
    }


    class Page{
        @NotNull(message = "L1不允许null")
        Long l1;
        @NotNull
        Long l2;
    }
}
