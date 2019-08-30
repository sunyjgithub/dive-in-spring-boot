package com.imooc.web.controller;

import com.imooc.web.aspect.CRUDRest;
import com.imooc.web.entity.Person;
import com.imooc.web.resp.BatchResp;
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
    @CRUDRest(type = CRUDRest.Type.Read)
    public String hello2(){
        return "hello";
    }







    @RequestMapping("/hello3")
    @ResponseBody
    @CRUDRest(type = CRUDRest.Type.Read)
    public BatchResp<Person> hello3(){

        Person p=new Person();
        p.setId(12);
        p.setName("sunyj");
        BatchResp batchResp=new BatchResp("2101231993");
        batchResp.setData(p);

        return batchResp ;
    }


    @RequestMapping("/add")
    @ResponseBody
    @CRUDRest(type = CRUDRest.Type.Create)
    public BatchResp<Person> add(@RequestBody Person person){
        BatchResp<Person> batchResp=new BatchResp();
        batchResp.setData(person);
        return batchResp;
    }


}
