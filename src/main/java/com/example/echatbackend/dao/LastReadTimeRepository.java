package com.example.echatbackend.dao;
import com.example.echatbackend.entity.LastReadTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LastReadTimeRepository extends JpaRepository<LastReadTime, Integer> {
    LastReadTime findByConversationIdAndUserId(String conversationId,Integer userId);
}
