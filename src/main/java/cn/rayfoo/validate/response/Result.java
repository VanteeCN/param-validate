package cn.rayfoo.validate.response;

import lombok.Builder;
import lombok.Data;


/**
 * @author rayfoo@qq.com
 * @date 2020年8月6日
 */
@Data@Builder
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */

    private String  msg;

    /**
     * 数据记录
     */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

}
