package cn.rayfoo.validate.exception;

import lombok.*;

/**
 * @Author: rayfoo@qq.com
 * @Date: 2020/7/20 9:26 下午
 * @Description: 自定义的异常...
 */
@Getter@Setter@Builder@NoArgsConstructor@AllArgsConstructor
public class MyException extends RuntimeException{

    private int code;

    private String msg;

}
