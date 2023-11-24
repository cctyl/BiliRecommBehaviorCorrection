package io.github.cctyl.config;



import io.github.cctyl.domain.dto.R;
import io.github.cctyl.utils.ExceptionUtil;
import io.github.cctyl.utils.ServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;



@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R error(Exception e){

        log.error("出现了异常，已经被全局异常处理捕获："+ ExceptionUtil.getMessage(e));

        return R.error().setMessage("意料之外的错误");
    }


    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    public R nullPointer(Exception e){
        log.error("出现空指针异常："+ExceptionUtil.getMessage(e));

        return R.error().setMessage("参数异常/数据不存在");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public R methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e){
        log.warn("出现了类型转换异常，已经被全局异常处理捕获："+ExceptionUtil.getMessage(e));

        return R.error().setMessage("意料之外的错误，你输入的数据格式不对");
    }


    @ExceptionHandler(ServerException.class)
    @ResponseBody
    public R guliException(ServerException e){
        String message = e.getMessage();
        Integer code = e.getCode();
        log.error("出现了自定义异常GuliException，已经被捕获："+ ExceptionUtil.getMessage(e));

        return R.error().setMessage(message).setCode(code);
    }


    /**
     * json解析异常
     * @param e
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public R jsonParseError(HttpMessageNotReadableException e) {
        log.error(e.getMessage());

        return R.error().setMessage("参数格式错误！").setCode(412);

    }
}
