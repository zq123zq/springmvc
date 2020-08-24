package com.zq.xspring.annotation;

/**
 * @author zq
 */

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XAutowired {
    String value() default "";
}
