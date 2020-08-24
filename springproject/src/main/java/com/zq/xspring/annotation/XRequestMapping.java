package com.zq.xspring.annotation;

/**
 * @author zq
 */

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XRequestMapping {
    String value() default "";
}
