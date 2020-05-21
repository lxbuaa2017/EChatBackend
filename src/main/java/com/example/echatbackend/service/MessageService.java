package com.example.echatbackend.service;

import com.example.echatbackend.dao.ConversationRepository;
import com.example.echatbackend.dao.FriendRepository;
import com.example.echatbackend.dao.GroupUserRepository;
import com.example.echatbackend.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final FriendRepository friendRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;

    @Autowired
    public MessageService(ConversationRepository conversationRepository, FriendRepository friendRepository,
                          GroupUserRepository groupUserRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.friendRepository = friendRepository;
        this.groupUserRepository = groupUserRepository;
        this.userRepository = userRepository;
    }

}
