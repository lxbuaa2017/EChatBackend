package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.Conversation;
import com.example.echatbackend.entity.Group;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@CrossOrigin
@RestController
public class UserController extends BaseController {

    private final CaptchaService captchaService;
    private final ConversationService conversationService;
    private final GroupService groupService;
    private final TokenService tokenService;
    private final UserService userService;

    @Autowired
    public UserController(CaptchaService captchaService, ConversationService conversationService,
                          GroupService groupService, TokenService tokenService,
                          UserService userService) {
        this.captchaService = captchaService;
        this.conversationService = conversationService;
        this.groupService = groupService;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @PostMapping("/user/register")
    public ResponseEntity<Object> register(@NotNull @RequestBody JSONObject request) {
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
        if (!captchaService.checkCaptcha(email, captcha)) {
            return requestFail(-1, "验证码错误或已失效");
        }
        User user = new User();
        user.randomSalt();
        user.setPassword(password);
        user.setEmail(email);
        user.setUserName(name);
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

    @PostMapping("/user/addConversation")
    public ResponseEntity<Object> addConversation(@NotNull @RequestBody JSONObject request) {
        User user = tokenService.getCurrentUser();
        Integer itemId = request.getInteger("itemId");
        if (itemId == null) {
            return requestFail(-1, "参数错误");
        }
        String type = request.getString("type");
        Conversation conversation;
        if (type.equals("friend")) {
            User friend = userService.findUserById(itemId);
            if (friend == null) {
                return requestFail(-1, "用户不存在");
            }
            conversation = conversationService.addConversation(friend);
            user.getConversationList().add(conversation);
            return requestSuccess();
        } else if (type.equals("group")) {
            Group group = groupService.findGroupById(itemId);
            if (group == null) {
                return requestFail(-1, "群组不存在");
            }
            conversation = conversationService.addConversation(group);
            user.getConversationList().add(conversation);
            return requestSuccess();
        } else {
            return requestFail(-1, "参数错误");
        }
    }

    @PostMapping("/user/removeConversation")
    public ResponseEntity<Object> removeConversation(@NotNull @RequestBody JSONObject request) {
        User user = tokenService.getCurrentUser();
        Conversation conversation;
        Integer id = request.getInteger("id");
        if (id == null) {
            return requestFail(-1, "参数错误");
        }
        try {
            conversation = conversationService.getOne(id);
        } catch (EntityNotFoundException e) {
            return requestFail(-1, "会话不存在");
        }
        List<Conversation> conversationList = user.getConversationList();
        if (conversationList.contains(conversation)) {
            conversationService.delete(conversation);
            return requestSuccess();
        } else {
            return requestFail(-1, "会话不存在");
        }
    }
}
