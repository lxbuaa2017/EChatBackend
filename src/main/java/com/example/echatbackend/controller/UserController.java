package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.CaptchaService;
import com.example.echatbackend.service.TokenService;
import com.example.echatbackend.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class UserController extends BaseController {

    private final CaptchaService captchaService;
    private final UserService userService;
    private final TokenService tokenService;

    @Autowired
    public UserController(CaptchaService captchaService, UserService userService, TokenService tokenService) {
        this.captchaService = captchaService;
        this.userService = userService;
        this.tokenService = tokenService;
    }


    @PostMapping("/user/login")
    public ResponseEntity<Object> login(@NotNull @RequestBody JSONObject request) {
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
        if (user.checkPassword(password) != true) {
            return requestFail(-1, "密码错误");
        }
        var response = new JSONObject();
        response.put("token", tokenService.createToken(user.getId()));
        response.put("userName", user.getUserName());
        return requestSuccess(0);
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

//    @PostMapping("/user/updateInfo")
//    public ResponseEntity<Object> updateInfo(@NotNull @RequestBody JSONObject request) {
//        String type = request.getString("type");
//        String content = request.getString("content");
//
//
//    }
}
