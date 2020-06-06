package com.example.echatbackend.dao;

import com.example.echatbackend.entity.Message;
import com.example.echatbackend.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer>, JpaSpecificationExecutor<Message> {
//    有个隐患，没考虑多条重复申请
    List<Message> findMessageByConversationIdAndUserM(String conversationId, User userM);
    List<Message> findAllByConversationId(String conversationId, PageRequest of);
    List<Message> findAllByConversationId(String conversationId);
}
