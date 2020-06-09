package com.example.echatbackend.service;

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

    private final ConversationRepository conversationRepository;
    private final FriendRepository friendRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(ConversationRepository conversationRepository, FriendRepository friendRepository,
                          GroupUserRepository groupUserRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.friendRepository = friendRepository;
        this.groupUserRepository = groupUserRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
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

    public void setReadStatus(User user,String conversationId) throws UnsupportedEncodingException {
        System.out.println("setReadStatus:"+user.getUserName());
        List<Message> messages = findAllConversationMessage(conversationId);
        String userName = user.getUserName();
        for (Message message : messages) {
            Set<User> readList = message.getReadList();
            Set<String> names = readList.stream().map(User::getUserName).collect(Collectors.toSet());
            boolean contains = false;
            for(String each:names){
                if(each.equals(userName)){
                    contains = true;
                    break;
                }
            }
            if (contains) {
                readList.add(user);
                messageRepository.saveAndFlush(message);
            }
        }
    }
}
