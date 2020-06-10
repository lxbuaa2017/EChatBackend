package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.Message;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.MessageService;
import com.example.echatbackend.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
public class MessageController extends BaseController {

    private final MessageService messageService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private TokenService tokenService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }


    // 删除信息
    @PostMapping("/mes/removeMessage")
    public ResponseEntity<Object> removeMessage(@RequestBody JSONObject request) {
        Integer id = request.getInteger("id");
        if (id == null) {
            return requestFail(-1, "参数错误");
        }
        messageService.deleteMessage(id);
        return requestSuccess();
    }

    // 切换窗口，加载更多消息
    @GetMapping("/mes/getMoreMessage")
    public ResponseEntity<Object> getMoreMessages(@RequestParam String conversationId,@RequestParam Integer offset,@RequestParam Integer limit) throws UnsupportedEncodingException {
        User user = tokenService.getCurrentUser();
        stringRedisTemplate.opsForValue().set(user.getUserName(),conversationId);


        if (offset == null || limit == null) {
            return requestFail(-1, "参数错误");
        }
        List<Message> messageList = messageService.getMoreMessage(conversationId, offset -1, limit,-1);
        ArrayList<Message> messages = new ArrayList<>(messageList);
        Collections.reverse(messages);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", messages.stream().map(Message::show).toArray(JSONObject[]::new));
        jsonObject.put("conversationId",conversationId);
        messageService.setReadStatus(user,conversationId);
        return requestSuccess(jsonObject);
    }

    // 当前窗口，加载更多消息
    @GetMapping("/mes/loadMoreMessage")
    public ResponseEntity<Object> loadMoreMessages(@RequestParam String conversationId,@RequestParam Integer offset,@RequestParam Integer limit) throws UnsupportedEncodingException {

        if (offset == null || limit == null) {
            return requestFail(-1, "参数错误");
        }
        List<Message> messageList = messageService.getMoreMessage(conversationId, offset -1, limit,-1);
        ArrayList<Message> messages = new ArrayList<>(messageList);
        Collections.reverse(messages);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", messages.stream().map(Message::show).toArray(JSONObject[]::new));
        jsonObject.put("conversationId",conversationId);
        return requestSuccess(jsonObject);
    }
}
