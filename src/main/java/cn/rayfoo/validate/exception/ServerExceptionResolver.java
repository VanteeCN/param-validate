package cn.rayfoo.validate.exception;

import cn.rayfoo.validate.response.HttpStatus;
import cn.rayfoo.validate.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * @date 2020/8/5 14:55
 * @description 全局异常处理
 */
@ControllerAdvice@Slf4j
public class ServerExceptionResolver {

    /**
     * 处理自定义的异常
     * @param ex
     * @return
     */
    @ExceptionHandler(MyException.class)@ResponseBody
    public Result<String> resolveMyException(MyException ex){
        //打印完整的异常信息
        ex.printStackTrace();
        //创建result
        Result<String> result = new Result<>();
        //设置result属性
        result.setData(ex.getMsg());
        result.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        result.setMsg(ex.getMsg());
        //保存自定义异常日志
        log.error(ex.getMsg());
        return result;
    }
}
