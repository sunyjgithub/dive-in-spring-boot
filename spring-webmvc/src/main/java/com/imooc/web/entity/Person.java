package com.imooc.web.entity;


import com.imooc.web.validation.Add;

import javax.validation.constraints.NotBlank;

public class Person {

    @NotBlank(groups = Add.class)
    private Integer id;

    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
