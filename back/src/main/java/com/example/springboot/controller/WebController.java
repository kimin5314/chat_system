package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.exception.CustomException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {
    @GetMapping("/hello")
    public Result hello(){
        return Result.success("hello");
    }
    @GetMapping("/count")
    public Result count(){
        return Result.success("10");
    }
    @GetMapping("/lop")
    public Result error(){
        throw new CustomException("400","error,don't access");
    }
}
