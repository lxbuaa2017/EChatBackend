package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.FriendService;
import com.example.echatbackend.service.TokenService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class FriendController extends BaseController {

    private final FriendService friendService;
    private final TokenService tokenService;

    @Autowired
    public FriendController(FriendService friendService, TokenService tokenService) {
        this.friendService = friendService;
        this.tokenService = tokenService;
    }

    // 查找我的好友
    @GetMapping("/friend/getMyfriend")
    public ResponseEntity<Object> getMyfriends() {
        User user = tokenService.getCurrentUser();
        return ResponseEntity.ok(friendService.findFriend(user.getId()));
    }

    // 验证是否已加为好友
    @GetMapping("/friend/checkMyfriend")
    public ResponseEntity<Object> checkMyfriend(@RequestParam Integer userid) {
        User user = tokenService.getCurrentUser();
        int myId = user.getId();
//        int yourId = Integer.parseInt(userid);
        int yourId = userid;
        JSONObject jsonObject = new JSONObject();
        if (friendService.checkFriend(myId, yourId)||friendService.checkFriend(yourId, myId)) {
            jsonObject.put("isMyfriend", true);
            return ResponseEntity.ok(jsonObject);
        }
        else{
            jsonObject.put("isMyfriend",false);
            return ResponseEntity.ok(jsonObject);
        }

    }

    //删除好友
    @DeleteMapping("/friend/deleteMyfriend")
    public ResponseEntity<Object> deleteMyfriend(@RequestBody JSONObject request) {
        User user = tokenService.getCurrentUser();
        int friendId = Integer.parseInt(request.getString("userid"));
        int res = friendService.deleteFriend(user, friendId);
        if (res == -1)
            return requestFail(-1, "fail to delete a friend");
        else
            return requestSuccess(0);
    }
}


