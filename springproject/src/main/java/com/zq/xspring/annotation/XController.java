package com.zq.xspring.annotation;

/**
 * @author zq
 */

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XController {
    String value() default "";
}
