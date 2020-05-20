package com.example.echatbackend.controller.reference;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.controller.BaseController;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.reference.CaptchaService;
import com.example.echatbackend.service.reference.SecurityService;
import com.example.echatbackend.service.reference.TokenService;
import com.example.echatbackend.service.reference.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class UserController extends BaseController {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@NotNull @RequestBody JSONObject request) {
        var data = securityService.decryptDataJSON(request);
        if (data == null)
            return new ResponseEntity<>("data", HttpStatus.BAD_REQUEST);
        var uuid = request.getString("uuid");
        var captcha = data.getString("captcha");
        var userName = data.getString("userName");
        var password = data.getString("password");
        if (captcha == null)
            return new ResponseEntity<>("captcha", HttpStatus.BAD_REQUEST);
        if (uuid == null)
            return new ResponseEntity<>("uuid", HttpStatus.BAD_REQUEST);
        if (userName == null)
            return new ResponseEntity<>("userName", HttpStatus.BAD_REQUEST);
        if (password == null)
            return new ResponseEntity<>("password", HttpStatus.BAD_REQUEST);
        if (!captchaService.checkCaptcha(uuid, captcha)) {
            return requestFail("验证码错误或已失效");
        }
        if (userService.findByUserName(userName) != null) {
            return requestFail("该用户名已被注册");
        }
        var user = new User();
        user.randomSalt();
        user.setPassword(password);
        user.setUserName(userName);
        try {
            return loginSuccess(userService.saveAndFlush(user));
        } catch (Exception e) {
            return requestFail("用户名已被注册");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@NotNull @RequestBody JSONObject request) {
        var data = securityService.decryptDataJSON(request);
        if (data == null)
            return new ResponseEntity<>("data", HttpStatus.BAD_REQUEST);
        var uuid = request.getString("uuid");
        var captcha = data.getString("captcha");
        var userName = data.getString("userName");
        var password = data.getString("password");
        if (captcha == null)
            return new ResponseEntity<>("captcha", HttpStatus.BAD_REQUEST);
        if (uuid == null)
            return new ResponseEntity<>("uuid", HttpStatus.BAD_REQUEST);
        if (userName == null)
            return new ResponseEntity<>("userName", HttpStatus.BAD_REQUEST);
        if (password == null)
            return new ResponseEntity<>("password", HttpStatus.BAD_REQUEST);
        if (!captchaService.checkCaptcha(uuid, captcha)) {
            return requestFail("验证码错误或已失效");
        }
        var user = userService.findByUserName(userName);
        if (user == null) {
            return requestFail("用户名不存在");
        }
        if (!user.checkPassword(password)) {
            return requestFail("密码错误");
        }
        return loginSuccess(user);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Object> changePassword(@NotNull @RequestBody JSONObject request) {
        var data = securityService.decryptDataJSON(request);
        if (data == null)
            return new ResponseEntity<>("data", HttpStatus.BAD_REQUEST);
        var uuid = request.getString("uuid");
        var captcha = data.getString("captcha");
        var userName = data.getString("userName");
        var password = data.getString("password");
        var oldPassword = data.getString("oldPassword");
        if (captcha == null)
            return new ResponseEntity<>("captcha", HttpStatus.BAD_REQUEST);
        if (uuid == null)
            return new ResponseEntity<>("uuid", HttpStatus.BAD_REQUEST);
        if (userName == null)
            return new ResponseEntity<>("userName", HttpStatus.BAD_REQUEST);
        if (password == null)
            return new ResponseEntity<>("password", HttpStatus.BAD_REQUEST);
        if (oldPassword == null)
            return new ResponseEntity<>("oldPassword", HttpStatus.BAD_REQUEST);
        if (!captchaService.checkCaptcha(uuid, captcha)) {
            return requestFail("验证码错误或已失效");
        }
        var user = userService.findByUserName(userName);
        if (user == null) {
            return requestFail("用户名不存在");
        }
        if (!user.checkPassword(oldPassword)) {
            return requestFail("密码错误");
        }
        user.setPassword(password);
        userService.saveAndFlush(user);
        return requestSuccess();
    }

    @PostMapping("/api/logout")
    public void logout() {
        tokenService.deleteToken(tokenService.getCurrentUser().getId());
    }

    private ResponseEntity<Object> loginSuccess(@NotNull User user) {
        var response = new JSONObject();
        response.put("token", tokenService.createToken(user.getId()));
        response.put("userName", user.getUserName());
        return requestSuccess(response);
    }
}
