package com.example.echatbackend.service;

import com.example.echatbackend.dao.GroupUserRepository;
import com.example.echatbackend.entity.GroupUser;
import org.springframework.stereotype.Service;

@Service
public class GroupUserService extends BaseService<GroupUser, Integer, GroupUserRepository> {
}
