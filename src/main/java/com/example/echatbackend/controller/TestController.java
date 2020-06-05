package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class TestController {
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/test")
    public void getMyfriends() {
        System.out.println(userRepository.findAll());
//        System.out.println(userRepository.findByUserName("ghj2726"));
    }
}
