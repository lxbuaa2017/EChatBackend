package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.Message;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.MessageService;
import com.example.echatbackend.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.alibaba.fastjson.JSON.toJSONString;

@CrossOrigin
@RestController
public class TestController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @GetMapping("/test/getMyfriend")
    public void getMyfriends() {
        System.out.println(userRepository.findAll());
//        System.out.println(userRepository.findByUserName("ghj2726"));
    }
    @GetMapping("/test/getUserInfo")
    public User getUserInfo(@RequestParam String id){
        int _id = Integer.valueOf(id);
        return userService.findUserById(_id);
    }

    @PostMapping("/test/getMoreMessage")
    public List<Message> getMoreMessage(){
        return  messageService.getMoreMessage("1-3",1,10,1);
    }
}
