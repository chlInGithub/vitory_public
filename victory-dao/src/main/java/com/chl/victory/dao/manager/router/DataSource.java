package com.chl.victory.dao.manager.router;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {

    String name() default DataSource.dsPromotion;

    String dsPromotion = "dsPromotion";
    String dsBusiness = "dsBusiness";
    String dsCfbidm = "dsCfbidm";
    String output = "output";
    String BASIC_LOW = "comm";
}
