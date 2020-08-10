package com.github.validate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p>对Map</p>
 * @date 2020/8/8 19:50
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestMap {

    /**
     * 实体类全类名列表
     */
    String[] baseEntityList();

}
