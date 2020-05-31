package com.example.echatbackend.dao;

import com.example.echatbackend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    //List<Integer> findAllByUserContains(User user);
}
