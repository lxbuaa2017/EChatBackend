package com.example.echatbackend.service;

import antlr.Token;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.example.echatbackend.dao.*;
import com.example.echatbackend.entity.Message;
import com.example.echatbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.toJSONString;

@Service
public class MessageService extends BaseService<Message, Integer, MessageRepository> {


    private final MessageRepository messageRepository;
    private final LastReadTimeService lastReadTimeService;
    private final TokenService tokenService;
    @Autowired
    public MessageService(ConversationRepository conversationRepository, FriendRepository friendRepository,
                          GroupUserRepository groupUserRepository, UserRepository userRepository, MessageRepository messageRepository
    ,LastReadTimeService lastReadTimeService,TokenService tokenService) {
        this.messageRepository = messageRepository;
        this.lastReadTimeService = lastReadTimeService;
        this.tokenService = tokenService;
    }

    public void deleteMessage(int id) {
        baseRepository.deleteById(id);
    }

    //reverse 按时间 1为正序，-1为倒序
    public List<Message> getMoreMessage(String conversationId, int offset, int limit, int reverse) {
        Specification<Message> messageSpecification = (Specification<Message>) (root, criteriaQuery, cb) -> cb.equal(root.get("conversationId"), conversationId);
        Sort sort;
        if (reverse == 1)
            sort = Sort.by(Sort.Order.asc("time"));
        else
            sort = Sort.by(Sort.Order.desc("time"));
        return baseRepository.findAll(messageSpecification, PageRequest.of(offset, limit, sort)).getContent();
    }

    public List<Message> findAllConversationMessage(String conversationId) {
        return messageRepository.findAllByConversationId(conversationId);
    }
    public List<Message> findAllConversationMessageByLastReadTime(String conversationId,Long time) {
        return messageRepository.findAllByConversationIdAndTimeAfter(conversationId,time);
    }
/*
todo 每次都对所有消息进行检查，效率过低。是否考虑设个效率高点的机制？
解决：已设置LastReadTime机制。
 */
    public void setReadStatus(String conversationId) throws UnsupportedEncodingException {
        User user = tokenService.getCurrentUser();
        Long lastReadTime = lastReadTimeService.getAndSetNewLastReadTime(conversationId,user.getId());
        System.out.println("setReadStatus:"+user.getUserName());
        List<Message> messages = findAllConversationMessageByLastReadTime(conversationId,lastReadTime);
        Integer userId = user.getId();
        for (Message message : messages) {
            Set<User> readList = message.getReadList();
            Set<Integer> ids = readList.stream().map(User::getId).collect(Collectors.toSet());
            boolean contains = false;
            for(Integer each:ids){
                if(each.equals(userId)){
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                readList.add(user);
                messageRepository.saveAndFlush(message);
            }
        }
    }
}
