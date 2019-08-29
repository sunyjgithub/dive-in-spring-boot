package com.imooc.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

/**
 * HelloWorld {@link Controller}
 *
 * @author 小马哥
 * @since 2018/5/20
 */
@Controller
public class HelloWorldController {

     @RequestMapping("/hello")
     public String hello(){
         return "hello";
     }


    @RequestMapping("/hello2")
    @ResponseBody
    public String hello2(){
        return "hello";
    }


}
