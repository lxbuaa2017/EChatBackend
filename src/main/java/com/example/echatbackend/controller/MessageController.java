package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.service.MessageService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class MessageController extends BaseController {

    private MessageService messageService;

    @Autowired
    public MessageController() {
        MessageService messageService;
    }


    // 删除信息
    @PostMapping("/mes/removeMessage")
    public void removeMessage(@NotNull @RequestBody JSONObject request) {
    }

    // 加载更多消息
    @PostMapping("/mes/loadMoreMessages")
    public ResponseEntity<Object> loadMoreMessages(@NotNull @RequestBody JSONObject request) {
        /*
        [{
            //message类的所有字段
        }]
         */
        return null;
    }
}
