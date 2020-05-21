package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.service.FriendService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class FriendController extends BaseController {

    @Autowired
    private FriendService friendService;

    // 查找我的好友
    @PostMapping("/friend/findMyfriends")
    public ResponseEntity<Object> findMyfriends(@NotNull @RequestBody JSONObject request) {
        /*
        [{
            createDate: string, // 加好友时间
            nickname: string, //昵称
            photo: string, //头像
            signature: string, //签名
            id: string, //id
            roomid: string //房间id
        }]
         */
        return null;
    }

    // 验证是否已加为好友
    @PostMapping("/friend/checkMyfriends")
    public ResponseEntity<Object> getGroupDetailed(@NotNull @RequestBody JSONObject request) {
        /*
        {
            isMyfriends: bool
        }
         */
        return null;
    }
}
