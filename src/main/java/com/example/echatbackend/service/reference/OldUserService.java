package com.example.echatbackend.service.reference;

import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class OldUserService extends BaseService<User, Integer, UserRepository> {
    public User findByUserName(String userName) {
        return baseRepository.findByUserName(userName);
    }
}
