package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.Message;
import com.example.echatbackend.service.MessageService;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@CrossOrigin
@RestController
public class MessageController extends BaseController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }


    // 删除信息
    @DeleteMapping("/mes/removeMessage")
    public ResponseEntity<Object> removeMessage(Integer id) {
        if (id == null) {
            return requestFail(-1, "参数错误");
        }
        messageService.deleteMessage(id);
        return requestSuccess();
    }

    // 加载更多消息
    @PostMapping("/mes/getMoreMessage")
    public ResponseEntity<Object> loadMoreMessages(@NotNull @RequestBody JSONObject request) {
        Integer conversationId = request.getInteger("conversationId");
        Integer offset = request.getInteger("offset");
        Integer limit = request.getInteger("limit");
        if (conversationId == null || offset == null || limit == null) {
            return requestFail(-1, "参数错误");
        }
        List<Message> messageList = messageService.getMoreMessage(conversationId, offset - 1, limit);
        JSONObject response = new JSONObject();
        response.put("data", messageList.stream().map(Message::show).toArray(JSONObject[]::new));
        return requestSuccess(response);
    }
}
