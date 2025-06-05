package com.example.springboot.exception;

import com.example.springboot.common.Result;
import org.apache.coyote.Request;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice("com.example.springboot.controller")
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody  //返回json格式串
    public Result error(Exception e){
        e.printStackTrace();   //打印错误
        return Result.error();
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody  //返回json格式串
    public Result error(CustomException e){
        e.printStackTrace();   //打印错误
        return Result.error(e.getCode(), e.getMsg());
    }
}
