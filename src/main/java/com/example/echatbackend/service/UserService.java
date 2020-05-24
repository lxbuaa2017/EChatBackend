package com.example.echatbackend.service;

import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService extends BaseService<User, Integer, UserRepository> {

    public User findUserById(int id) {
        Optional<User> user = baseRepository.findById(id);
        if (user.isEmpty()) {
            return null;
        }
        return user.get();
    }

    public User findUserByName(String username) {
        return baseRepository.findByUserName(username);
    }
}
