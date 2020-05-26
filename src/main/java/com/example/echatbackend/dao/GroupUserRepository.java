package com.example.echatbackend.dao;

import com.example.echatbackend.entity.Group;
import com.example.echatbackend.entity.GroupUser;
import com.example.echatbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUser, Integer> {
    List<GroupUser> findAllByGroup(Group group);

    List<GroupUser> findAllByUser(User user);

}
