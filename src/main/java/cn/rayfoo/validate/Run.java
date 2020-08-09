package cn.rayfoo.validate;

import cn.rayfoo.validate.annotation.EnableVerify;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/9 21:35
 */
@EnableVerify(execution = "execution(* cn.rayfoo.web..*(..))")
@SpringBootApplication
public class Run {
    public static void main(String[] args) {
        SpringApplication.run(Run.class, args);
    }
}
