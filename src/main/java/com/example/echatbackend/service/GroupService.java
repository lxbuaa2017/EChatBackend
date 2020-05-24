package com.example.echatbackend.service;

import com.example.echatbackend.dao.GroupRepository;
import com.example.echatbackend.dao.GroupUserRepository;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GroupService extends BaseService<Group, Integer, GroupRepository> {

    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupService(GroupUserRepository groupUserRepository, UserRepository userRepository) {
        this.groupUserRepository = groupUserRepository;
        this.userRepository = userRepository;
    }

    public Group findGroupById(int id) {
        Optional<Group> group = baseRepository.findById(id);
        if (group.isEmpty()) {
            return null;
        }
        return group.get();
    }

}
