package com.example.echatbackend.service;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.dao.FriendRepository;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.Friend;
import com.example.echatbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Autowired
    public FriendService(FriendRepository friendRepository, UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    public JSONObject findFriend(User user) {
        List<Friend> friend1 = friendRepository.findAllByUserM(user);
        List<Friend> friend2 = friendRepository.findAllByUserY(user);
        Set<User> friends = friend1.stream().map(Friend::getUserY).collect(Collectors.toSet());
        friend2.forEach(friend -> friends.add(friend.getUserM()));
        friends.remove(user);
        JSONObject res = new JSONObject();
        res.put("data", friends.stream().map(User::show).toArray(JSONObject[]::new));
        return res;
    }

    public boolean checkFriend(User user, int userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }
        return (friendRepository.findByUserMAndUserY(user, optionalUser.get()) != null) ||
                (friendRepository.findByUserMAndUserY(optionalUser.get(), user) != null);
    }

    public boolean deleteFriend(User user, int friendId) {
        Optional<User> optionalUser = userRepository.findById(friendId);
        if (optionalUser.isEmpty()) {
            return false;
        }
        Friend friendRelationship;
        friendRelationship = friendRepository.findByUserMAndUserY(user, optionalUser.get());
        if (friendRelationship == null) {
            friendRelationship = friendRepository.findByUserMAndUserY(optionalUser.get(), user);
        }
        if (friendRelationship != null) {
            friendRepository.delete(friendRelationship);
            return true;
        } else {
            return false;
        }
    }

    public void addFriend(int userMid, int userYid) {
        Optional<User> optionalUserM = userRepository.findById(userMid);
        Optional<User> optionalUserY = userRepository.findById(userYid);
        if (optionalUserM.isPresent() && optionalUserY.isPresent()) {
            Friend friend = new Friend(optionalUserM.get(), optionalUserY.get());
            friendRepository.saveAndFlush(friend);
        }
    }
}
