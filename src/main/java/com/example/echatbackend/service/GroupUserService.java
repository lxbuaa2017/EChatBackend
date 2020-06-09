package com.example.echatbackend.service;

import com.example.echatbackend.dao.GroupUserRepository;
import com.example.echatbackend.entity.Group;
import com.example.echatbackend.entity.GroupUser;
import com.example.echatbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class GroupUserService extends BaseService<GroupUser, Integer, GroupUserRepository> {
    @Autowired
    private GroupUserRepository groupUserRepository;
    public boolean checkIfInGroup(Group group, User user){
        GroupUser optionalGroupUser = groupUserRepository.findByGroupAndUser(group,user);
        return optionalGroupUser != null;
    }
}
