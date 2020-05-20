package com.example.studentinfo.service.reference;

import com.example.studentinfo.dao.reference.UserRepository;
import com.example.studentinfo.entity.User;
import com.example.studentinfo.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseService<User, Integer, UserRepository> {
    public User findByUserName(String userName) {
        return baseRepository.findByUserName(userName);
    }
}
