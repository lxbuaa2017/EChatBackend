package com.example.echatbackend.service;

import com.example.echatbackend.dao.*;
import com.example.echatbackend.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class MessageService extends BaseService<Message, Integer, MessageRepository> {

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

    public void deleteMessage(int id) {
        baseRepository.deleteById(id);
    }

    public Page<Message> getMoreMessage(int roomId, int offset, int limit) {
        Specification<Message> messageSpecification = (Specification<Message>) (root, criteriaQuery, cb) -> cb.equal(root.get("roomId"), roomId);
        Sort sort = Sort.by(Sort.Order.asc("name"));
        Page<Message> messages = baseRepository.findAll(messageSpecification, PageRequest.of(offset, limit, sort));
        return messages;
    }
}
