package com.example.echatbackend.service;

import com.example.echatbackend.dao.ConversationRepository;
import com.example.echatbackend.entity.Conversation;
import com.example.echatbackend.entity.Group;
import com.example.echatbackend.entity.User;
import org.springframework.stereotype.Service;

@Service
public class ConversationService extends BaseService<Conversation, Integer, ConversationRepository> {

    public Conversation addConversation(User user) {
        Conversation conversation = new Conversation();
        conversation.setType("friend");
        conversation.setUser(user);
        baseRepository.saveAndFlush(conversation);
        return conversation;
    }

    public Conversation addConversation(Group group) {
        Conversation conversation = new Conversation();
        conversation.setType("group");
        conversation.setGroup(group);
        baseRepository.saveAndFlush(conversation);
        return conversation;
    }
}
