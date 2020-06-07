package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.Message;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.FriendService;
import com.example.echatbackend.service.MessageService;
import com.example.echatbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class TestController extends BaseController {
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
        var optionalUser = userRepository.findById(2);
        if (optionalUser.isEmpty()) {
            return requestFail(-1, "???");
        }
        return ResponseEntity.ok(friendService.findFriend(optionalUser.get()));
//        System.out.println(userRepository.findByUserName("ghj2726"));
    }

    @GetMapping("/test/getUserInfo")
    public User getUserInfo(@RequestParam String id) {
        int _id = Integer.parseInt(id);
        return userService.findUserById(_id);
    }

    @PostMapping("/test/getMoreMessage")
    public ResponseEntity<Object> getMoreMessage() {
        var messageList = messageService.getMoreMessage("1-3", 0, 10, 1);
        JSONObject response = new JSONObject();
        response.put("data", messageList.stream().map(Message::show).toArray(JSONObject[]::new));
        return requestSuccess(response);
    }
}
