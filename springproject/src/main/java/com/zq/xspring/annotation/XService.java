package com.zq.xspring.annotation;

/**
 * @author zq
 */

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XService {
    String value() default "";
}
