package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.dao.ConversationRepository;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.Conversation;
import com.example.echatbackend.entity.Message;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.FriendService;
import com.example.echatbackend.service.MessageService;
import com.example.echatbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
public class TestController extends BaseController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private FriendService friendService;

    @Autowired
    private ConversationRepository conversationRepository;

    @GetMapping("/test/getMyfriend")
    public ResponseEntity<Object> getMyfriends() {
        var optionalUser = userRepository.findById(2);
        if (optionalUser.isEmpty()) {
            return requestFail(-1, "???");
        }
        return ResponseEntity.ok(friendService.findFriend(optionalUser.get()));
//        System.out.println(userRepository.findByUserName("ghj2726"));
    }

    @GetMapping("/test/getUserInfo")
    public User getUserInfo(@RequestParam String id) {
        int _id = Integer.parseInt(id);
        return userService.findUserById(_id);
    }

    @GetMapping("/test/getMoreMessage")
    public ResponseEntity<Object> getMoreMessage() {
        long startTime = System.currentTimeMillis();    //获取结束时间//放在所测的代码下面
        List<Message> messageList = messageService.getMoreMessage("1-2", 0, 40, -1);
        ArrayList<Message> messages = new ArrayList<>(messageList);
        Collections.reverse(messages);
        JSONObject response = new JSONObject();
        response.put("data", messages.stream().map(Message::show).toArray(JSONObject[]::new));
        long endTime = System.currentTimeMillis();    //获取结束时间//放在所测的代码下面
        response.put("running_time",endTime - startTime);
        return requestSuccess(response);
    }

    @GetMapping("/test/mes/getMoreMessage")
    public ResponseEntity<Object> loadMoreMessages(@RequestParam String conversationId,@RequestParam Integer offset,@RequestParam Integer limit) {
        if (conversationId == null || offset == null || limit == null) {
            return requestFail(-1, "参数错误");
        }
        List<Message> messageList = messageService.getMoreMessage(conversationId, offset -1, limit,-1);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", messageList.stream().map(Message::show).toArray(JSONObject[]::new));
        jsonObject.put("conversationId",conversationId);
        return requestSuccess(jsonObject);
    }

    // 切换窗口
    @GetMapping("/test/setConversation")
    public ResponseEntity<Object> getMoreMessages() throws UnsupportedEncodingException {
        User user = userRepository.findByUserName("lx2020");
        messageService.setReadStatus(user,"123");
        return requestSuccess(0);
    }

    @GetMapping("/test/removeConversation")
    public ResponseEntity<Object> removeConversation() {
        User user = userRepository.findByUserName("ghj2726");
        Conversation conversation;
        String id = "1-2";
        if (id == null) {
            return requestFail(-1, "参数错误");
        }
        try {
            conversation = conversationRepository.findByConversationId(id);
        } catch (EntityNotFoundException e) {
            return requestFail(-1, "会话不存在");
        }
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
}
