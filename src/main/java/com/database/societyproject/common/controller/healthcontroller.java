package com.database.societyproject.common.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class healthcontroller {
    @GetMapping
    public String health(){
        return "hello";
    }
}
