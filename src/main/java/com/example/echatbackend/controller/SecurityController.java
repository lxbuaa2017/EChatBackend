package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.CaptchaService;
import com.example.echatbackend.service.EmailCaptchaService;
import com.example.echatbackend.service.reference.SecurityService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@CrossOrigin
@RestController
public class SecurityController extends BaseController {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private EmailCaptchaService emailCaptchaService;

    @GetMapping("/publicKey")
    public ResponseEntity<JSONObject> getPublicKey() {
        var keyPair = securityService.generateKeyPair();
        var response = new JSONObject();
        response.put("key", securityService.saveKeyForBase64(keyPair.getPublic()));
        response.put("uuid", securityService.createUuidForPrivateKey(securityService.saveKeyForBase64(keyPair.getPrivate())));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/captcha.jpg")
    public void getImageCaptcha(@NotNull HttpServletResponse response, String username, String useless) throws IOException {
        if (username == null) {
            response.setStatus(400);
            return;
        }
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        var image = captchaService.createCaptcha(username);
        var out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    @PostMapping("sendEmailCaptcha")
    public ResponseEntity<Object> sendEmailCaptcha(@NotNull @RequestBody JSONObject request) {
        var email = User.emailFormat(request.getString("email"));
        if (email == null) {
            return new ResponseEntity<>("email", HttpStatus.BAD_REQUEST);
        }
        emailCaptchaService.sendCaptcha(email);
        return requestSuccess();
    }
}
