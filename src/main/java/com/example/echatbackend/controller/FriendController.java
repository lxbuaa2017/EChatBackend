package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.FriendService;
import com.example.echatbackend.service.TokenService;
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
        return requestSuccess(friendService.findFriend(user));
    }

    // 验证是否已加为好友
    @GetMapping("/friend/checkMyfriend")
    public ResponseEntity<Object> checkMyfriend(@RequestParam Integer userid) {
        User user = tokenService.getCurrentUser();
        JSONObject jsonObject = new JSONObject();
        if (friendService.checkFriend(user, userid)) {
            jsonObject.put("isMyfriend", true);
        } else {
            jsonObject.put("isMyfriend", false);
        }
        return ResponseEntity.ok(jsonObject);
    }

    //删除好友
    @PostMapping("/friend/deleteMyfriend")
    public ResponseEntity<Object> deleteMyfriend(@RequestBody JSONObject request) {
        User user = tokenService.getCurrentUser();
        Integer friendId = request.getInteger("userid");
        if (friendId == null) {
            return requestFail(-1, "参数错误");
        }
        if (friendService.deleteFriend(user, friendId)) {
            return requestSuccess(0);
        } else {
            return requestFail(-1, "删除失败，好友不存在");
        }
    }
}