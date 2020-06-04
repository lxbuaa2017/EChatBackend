package com.example.echatbackend.dao;

import com.example.echatbackend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer>, JpaSpecificationExecutor<Message> {
    Message findMessageByConversationIdAndUserMName(Integer conversationId,String userMName);
}
