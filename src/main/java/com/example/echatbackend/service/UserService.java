package com.example.echatbackend.service;

import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseService<User, Integer, UserRepository> {

    public User findUserByName(String username) {
        return baseRepository.findByUserName(username);
    }
}
