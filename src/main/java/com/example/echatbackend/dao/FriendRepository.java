package com.example.echatbackend.dao;

import com.example.echatbackend.entity.Friend;
import com.example.echatbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Integer> {
    Friend findByUserMAndUserY(User userM, User userY);
}
