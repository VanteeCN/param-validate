package com.github.validate.annotation;

import com.github.validate.enums.RegexOption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/7 15:33
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.PARAMETER})
public @interface Verify {

    /** 参数名称 */
    String name();

    /** 参数最大长度 */
    int maxLength() default Integer.MAX_VALUE;

    /** 是否必填 这里只是判断是否为null */
    boolean required() default true;

    /** 是否为非空 是否为null和空串都判断 */
    boolean notNull() default true;

    /** 最小长度 */
    int minLength() default Integer.MIN_VALUE;

    /** 正则匹配 */
    RegexOption regular() default RegexOption.DEFAULT;

}
