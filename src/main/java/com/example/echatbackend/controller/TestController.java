package com.example.echatbackend.controller;

import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.Message;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.FriendService;
import com.example.echatbackend.service.MessageService;
import com.example.echatbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
public class TestController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private FriendService friendService;

    @GetMapping("/test/getMyfriend")
    public ResponseEntity<Object> getMyfriends() {
        return ResponseEntity.ok(friendService.findFriend(2));
//        System.out.println(userRepository.findByUserName("ghj2726"));
    }

    @GetMapping("/test/getUserInfo")
    public User getUserInfo(@RequestParam String id) {
        int _id = Integer.valueOf(id);
        return userService.findUserById(_id);
    }

    @PostMapping("/test/getMoreMessage")
    public List<Message> getMoreMessage() {
        return messageService.getMoreMessage("1-3", 1, 10, 1);
    }
}
