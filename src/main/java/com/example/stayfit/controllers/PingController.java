package com.example.stayfit.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {
    @RequestMapping(method = RequestMethod.GET,value = "/api/public/ping")
    public String ping(){
        return "pong";
    }
}
