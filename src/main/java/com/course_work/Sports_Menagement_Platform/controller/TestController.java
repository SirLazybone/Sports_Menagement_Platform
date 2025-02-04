package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class TestController {
    @Autowired
    private UserServiceImpl userService;
    @GetMapping("/admin")
    public String admin() {
        System.out.println("Hello from admin");
        return "This is admin";
    }

    @GetMapping("/user")
    public String user() {
        return "This is authenticated";
    }

    @GetMapping("/home")
    public String home() {
        return "This is home";
    }

    @GetMapping("/test")
    public String test() {
        return "test works!";
    }

    @GetMapping("path")
    public String getMethodName(@RequestParam String param) {
        return new String();
    }


    @GetMapping("/test2")
    public String test2() {
        return "test2";
    }

    @GetMapping("/test3")
    public String test3() {
        return "test3";
    }

}
