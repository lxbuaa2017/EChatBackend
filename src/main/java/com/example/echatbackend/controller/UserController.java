package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.Conversation;
import com.example.echatbackend.entity.Group;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
public class UserController extends BaseController {

    private final CaptchaService captchaService;
    private final ConversationService conversationService;
    private final GroupService groupService;
    private final TokenService tokenService;
    private final UserService userService;
    private final EmailCaptchaService emailCaptchaService;
    private final MessageService messageService;
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    public UserController(CaptchaService captchaService, ConversationService conversationService,
                          GroupService groupService, TokenService tokenService,
                          UserService userService, EmailCaptchaService emailCaptchaService,MessageService messageService,
                          StringRedisTemplate stringRedisTemplate) {
        this.captchaService = captchaService;
        this.conversationService = conversationService;
        this.groupService = groupService;
        this.tokenService = tokenService;
        this.userService = userService;
        this.emailCaptchaService = emailCaptchaService;
        this.messageService=messageService;
        this.stringRedisTemplate=stringRedisTemplate;

    }


    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody JSONObject request) {
        String name = request.getString("name");
        String password = request.getString("password");

        if (name == null) {
            return new ResponseEntity<>("name", HttpStatus.BAD_REQUEST);
        }
        if (password == null) {
            return new ResponseEntity<>("password", HttpStatus.BAD_REQUEST);
        }

        if (userService.findUserByName(name) == null) {
            return requestFail(-1, "用户名不存在");
        }
        User user = userService.findUserByName(name);
        if (!user.checkPassword(password)) {
            return requestFail(-1, "密码错误");
        }
        var response = new JSONObject();
        response.put("token", tokenService.createToken(user.getId()));
        response.put("nickname", user.getNickname());
        response.put("avatar", user.getAvatar());
        return requestSuccess(response);
    }

    @GetMapping("/sendEmail")
    public ResponseEntity<Object> sendEmail(String email) {
        if (email == null) {
            return new ResponseEntity<>("email", HttpStatus.BAD_REQUEST);
        }
        try {
            emailCaptchaService.sendCaptcha(email);
        } catch (Exception e) {
            return requestFail(-1, "发送邮件失败");
        }
        return requestSuccess(0);
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody JSONObject request) {
        String name = request.getString("name");
        String password = request.getString("password");
        String email = User.emailFormat(request.getString("email"));
        String captcha = request.getString("captcha");
        if (name == null) {
            return new ResponseEntity<>("name", HttpStatus.BAD_REQUEST);
        }
        if (password == null) {
            return new ResponseEntity<>("password", HttpStatus.BAD_REQUEST);
        }
        if (email == null) {
            return new ResponseEntity<>("email", HttpStatus.BAD_REQUEST);
        }
        if (captcha == null) {
            return new ResponseEntity<>("captcha", HttpStatus.BAD_REQUEST);
        }
        if (!emailCaptchaService.checkCaptcha(email, captcha)) {
            return requestFail(-1, "邮箱验证码错误");
        }
        User user = new User();
        user.randomSalt();
        user.setPassword(password);
        user.setEmail(email);
        user.setUserName(name);
        user.setNickname(name);
        /*if (userService.findUserByName(name) != null) {
            return requestFail(-1, "用户名已被注册");
        }*/
        try {
            userService.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            return requestFail(-1, "用户名或邮箱已被注册");
        }
        return requestSuccess(0);
    }

    @PostMapping("/user/updateInfo")
    public ResponseEntity<Object> updateInfo(@RequestBody JSONObject request) {
        User user = tokenService.getCurrentUser();
        String type = request.getString("type");
        String content = request.getString("content");
        if (type == null) {
            return new ResponseEntity<>("type", HttpStatus.BAD_REQUEST);
        }
        if (type.equals("nickname")) {
            user.setNickname(content);
        }
        if (type.equals("cover")) {
            user.setNickname(content);
        }
        if (type.equals("gender")) {
            user.setNickname(content);
        }
        if (type.equals("chatColor")) {
            user.setNickname(content);
        }
        return requestSuccess(0);
    }

    @PostMapping("/user/addConversation")
    public ResponseEntity<Object> addConversation(@RequestBody JSONObject request) {
        User user = tokenService.getCurrentUser();
        Integer userMId = user.getId();
        Integer itemId = request.getInteger("itemId");
        if (itemId == null) {
            return requestFail(-1, "参数错误");
        }
        String type = request.getString("type");
        Conversation conversation;
        String conversationId = "";
        if (type.equals("friend")) {
            User friend = userService.findUserById(itemId);
            conversationId = userMId<itemId?userMId+"-"+itemId:itemId+"-"+userMId;
            conversation = conversationService.findByConversationId(conversationId);
            user.getConversationList().add(conversation);
            userService.saveAndFlush(user);
            JSONObject data = new JSONObject();
            data.put("name",friend.getUserName());
            data.put("avatar",friend.getAvatar());
            data.put("id",conversationId);
            data.put("itemId",itemId);
            data.put("type",type);
            JSONObject res = new JSONObject();
            res.put("data",data);
            return requestSuccess(res);
        } else if (type.equals("group")) {
            conversation = conversationService.findByConversationId(itemId.toString());
            Group group = groupService.findGroupById(itemId);
            user.getConversationList().add(conversation);
            userService.saveAndFlush(user);
            JSONObject data = new JSONObject();
            data.put("name",group.getName());
            data.put("avatar",group.getAvatar());
            data.put("id",itemId.toString());
            data.put("itemId",itemId);
            data.put("type",type);
            JSONObject res = new JSONObject();
            res.put("data",data);
            return requestSuccess(res);
        } else {
            return requestFail(-1, "参数错误");
        }
    }

    @PostMapping("/user/removeConversation")
    public ResponseEntity<Object> removeConversation(@RequestBody JSONObject request) {
        User user = tokenService.getCurrentUser();
        Conversation conversation;
        String id = request.getString("id");
        if (id == null) {
            return requestFail(-1, "参数错误");
        }
//        try {
//            conversation = conversationService.findByConversationId(id);
//        } catch (EntityNotFoundException e) {
//            return requestFail(-1, "会话不存在");
//        }
        List<Conversation> conversations = user.getConversationList();
        conversations.removeIf(each -> each.getConversationId().equals(id));
        userService.saveAndFlush(user);
        return requestSuccess();
        //过分判断会增加时延，前端已保证逻辑，无须判断
//        List<Conversation> conversationList = user.getConversationList();
//        if (conversationList.contains(conversation)) {
//            conversationService.delete(conversation);
//            return requestSuccess();
//        } else {
//            return requestFail(-1, "会话不存在");
//        }
    }

    @GetMapping("/user/logout")
    public ResponseEntity<Object> logout() {
        tokenService.deleteToken(tokenService.getCurrentUser().getId());
        return requestSuccess(0);
    }

    @GetMapping("/user/getUserInfo")
    public ResponseEntity<Object> getUserInfo() {
        User currentUser = tokenService.getCurrentUser();
        JSONObject userInfo = new JSONObject();
        userInfo.put("name", currentUser.getUserName());
        userInfo.put("avatar", currentUser.getAvatar());
        userInfo.put("wallpaper", currentUser.getWallpaper());
        userInfo.put("nickname", currentUser.getNickname());
        userInfo.put("signature", currentUser.getSignature());
        userInfo.put("gender", currentUser.getGender());
        userInfo.put("id", currentUser.getId());
        userInfo.put("bgOpa", currentUser.getBgOpa());
        userInfo.put("conversationsList", currentUser.getConversationList().stream().map(conversation -> conversation.show(currentUser.getId())).toArray(JSONObject[]::new));
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("data", userInfo);
        return requestSuccess(result);
    }

    @GetMapping("/user/getOtherUserInfo")
    public ResponseEntity<Object> getOtherUserInfo(@RequestParam Integer id) {
        User currentUser = userService.findUserById(id);
        JSONObject userInfo = new JSONObject();
        userInfo.put("name", currentUser.getUserName());
        userInfo.put("avatar", currentUser.getAvatar());
        userInfo.put("wallpaper", currentUser.getWallpaper());
        userInfo.put("nickname", currentUser.getNickname());
        userInfo.put("signature", currentUser.getSignature());
        userInfo.put("gender", currentUser.getGender());
        userInfo.put("id", currentUser.getId());
        userInfo.put("bgOpa", currentUser.getBgOpa());
        userInfo.put("conversationsList", currentUser.getConversationList().stream().map(conversation -> conversation.show(currentUser.getId())).toArray(JSONObject[]::new));
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("data", userInfo);
        return requestSuccess(result);
    }

    @GetMapping("/user/getSystemUserInfo")
    public ResponseEntity<Object> getSystemUserInfo() {
        User currentUser = userService.findUserById(3);
        JSONObject userInfo = new JSONObject();
        userInfo.put("name", currentUser.getUserName());
        userInfo.put("avatar", currentUser.getAvatar());
        userInfo.put("wallpaper", currentUser.getWallpaper());
        userInfo.put("nickname", currentUser.getNickname());
        userInfo.put("signature", currentUser.getSignature());
        userInfo.put("gender", currentUser.getGender());
        userInfo.put("id", currentUser.getId());
        userInfo.put("bgOpa", currentUser.getBgOpa());
        userInfo.put("conversationsList", currentUser.getConversationList().stream().map(conversation -> conversation.show(currentUser.getId())).toArray(JSONObject[]::new));
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("data", userInfo);
        return requestSuccess(result);
    }

    @PostMapping("/user/updateUserInfo")
    public ResponseEntity<Object> updateUserInfo(@RequestBody JSONObject request) {
        User currentUser = tokenService.getCurrentUser();
        String type = request.getString("type");
        String content = request.getString("content");
        switch (type) {
            case "nickname":
                currentUser.setNickname(content);
                break;
            case "signature":
                currentUser.setSignature(content);
                break;
            case "avatar":
                currentUser.setAvatar(content);
                break;
            case "wallpaper":
                currentUser.setWallpaper(content);
                break;
            case "chatColor":
                currentUser.setChatColor(content);
                break;
            default:
                return requestFail(-1, "请指定正确的字段名");
        }
        userService.save(currentUser);
        return requestSuccess(0);
    }

    @PostMapping("/user/searchFriend")
    public ResponseEntity<Object> searchFriend(@RequestBody JSONObject request) {
        String keyword = request.getString("keyword");
        Integer offset = request.getInteger("offset");
        Integer limit = request.getInteger("limit");
        Integer type = request.getInteger("type");
        List<User> userList;
        if (keyword == null || offset == null || limit == null || type == null) {
            return requestFail(-1, "参数错误");
        }
        keyword = keyword.trim();
        if (type == 1) {
            userList = userService.searchUserByName(keyword, offset - 1, limit);
        } else if (type == 2) {
            userList = userService.searchUserByNickname(keyword, offset - 1, limit);
        } else {
            return requestFail(-1, "参数错误");
        }
        JSONObject response = new JSONObject();
        response.put("data", userList.stream().map(User::show).toArray());
        return requestSuccess(response);
    }

    @GetMapping("/user/updateUserGender")
    public ResponseEntity<Object> updateUserGender(@RequestParam Integer gender) {
        User user = tokenService.getCurrentUser();
        if (gender != 0 && gender != 1 && gender != 2)
            return requestFail(-1, "第四性别不存在");
        else {
            user.setGender(gender);
            userService.save(user);
            return requestSuccess(0);
        }
    }

    @GetMapping("/user/updateBgOpa")
    public ResponseEntity<Object> updateBgOpa(@RequestParam double bgOpa) {
        User user = tokenService.getCurrentUser();
        user.setBgOpa(bgOpa);
        userService.save(user);
        return requestSuccess(0);
    }

    // 切换窗口
    @GetMapping("/user/setConversation")
    public ResponseEntity<Object> getMoreMessages(@RequestParam String conversationId) throws UnsupportedEncodingException {
        User user = tokenService.getCurrentUser();
        stringRedisTemplate.opsForValue().set(user.getUserName(),conversationId);
        messageService.setReadStatus(user,conversationId);
        return requestSuccess(0);
    }
}
