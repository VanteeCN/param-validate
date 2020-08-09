package cn.rayfoo.validate.annotation;

import cn.rayfoo.validate.config.EnableVerifyConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/9 13:36
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({EnableVerifyConfig.class})
public @interface EnableVerify {

    String execution();

}
