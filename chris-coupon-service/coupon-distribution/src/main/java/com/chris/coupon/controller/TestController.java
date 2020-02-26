package com.chris.coupon.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/distribution/welcome")
    public String messageFromDistribution(){
        return "Welcome to Distribution";
    }
}

