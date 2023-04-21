package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;


/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/2/28 16:45
 */


@Slf4j
@ControllerAdvice//控制器增强
public class GlobalExceptionHandler {

    //处理XueChengPlusException，主动抛出，可预知
    @ResponseBody//返回json数据
    @ExceptionHandler(XueChengPlusException.class)//此方法捕获XueChengPlusException
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//状态码返回500
    public RestErrorResponse doXueChengPlusException(XueChengPlusException e){
        log.error("捕获异常：{}", e.getErrMessage());
        e.printStackTrace();

        String errMessage = e.getErrMessage();

        return new RestErrorResponse(errMessage);
    }

    //捕获不可预知的异常 Exception
    @ResponseBody//返回json数据
    @ExceptionHandler(Exception.class)//此方法捕获XueChengPlusException
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//状态码返回500
    public RestErrorResponse doException(Exception e){
        log.error("捕获异常：{}", e.getMessage());
        if (e.getMessage().equals("不允许访问")) {
            return new RestErrorResponse("您没有权限操作此方法");
        }
        e.printStackTrace();

        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

    //捕获参数检验异常 MethodArgumentNotValidException
    @ResponseBody//返回json数据
    @ExceptionHandler(MethodArgumentNotValidException.class)//此方法捕获MethodArgumentNotValidException
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//状态码返回500
    public RestErrorResponse doMethodArgumentNotValidException(MethodArgumentNotValidException e){

        BindingResult bindingResult = e.getBindingResult();

        StringBuffer errMessage = new StringBuffer();

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error ->{
            errMessage.append(error.getDefaultMessage()).append(",");
        });

        log.error(errMessage.toString());

        return new RestErrorResponse(errMessage.toString());
    }

}
