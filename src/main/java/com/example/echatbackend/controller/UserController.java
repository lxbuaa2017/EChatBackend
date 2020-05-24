package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.CaptchaService;
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

    @Autowired
    public UserController(CaptchaService captchaService, UserService userService) {
        this.captchaService = captchaService;
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


    /*
        req:
        {
        name: string//用户名
        password: string//密码
        email: string//邮箱
        captcha: string//验证码
        }
        res:
        //注册成功
        {
        code:0
        name: string//用户名
        }
        //注册失败
        {
        code:-1
        msg: string//用户名重复、验证码错误等
        }
     */

}
