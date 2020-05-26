package com.example.echatbackend.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class EmailCaptchaService {

    private final String emailFrom = "1134220742@qq.com";

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void sendCaptcha(@NotNull String emailTo) {
        var captcha = new Random().nextInt(899999) + 100000;
        var captcha_str = Integer.toString(captcha);
        var message = new SimpleMailMessage();
        message.setTo(emailTo);
        message.setSubject("验证码");
        message.setText("您好！欢迎注册成为 EChat 用户，您本次注册的验证码是：" + captcha_str
                + "，十分钟内有效。如果不是您本人的操作，请忽略此邮件；请勿将验证码告诉他人。");
        message.setFrom(emailFrom);
        mailSender.send(message);
        System.out.println(emailTo);
        System.out.println(captcha_str);
        stringRedisTemplate.opsForValue().set(emailTo, captcha_str, 10, TimeUnit.MINUTES);
    }

    public boolean checkCaptcha(@NotNull String to, @NotNull String captcha_str) {
        var check_pass = captcha_str.equals(stringRedisTemplate.opsForValue().get(to));
        if (check_pass) {
            stringRedisTemplate.delete(to);
        }
        return check_pass;
    }
}
