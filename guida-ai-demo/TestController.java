package com.example.guidaaibackend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello AI! 我的第一个 Java 接口跑通了！";
    }
}