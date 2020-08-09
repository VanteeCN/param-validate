package com.github.validate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/8 22:43
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestEntity {

    String value() default "";

}
