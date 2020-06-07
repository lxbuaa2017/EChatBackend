package com.example.echatbackend.service;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.dao.FriendRepository;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.Friend;
import com.example.echatbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.IntFunction;
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


    public JSONObject findFriend(int userId) {
            User user = userRepository.findById(userId).get();
            List<Friend> friend1 = friendRepository.findAllByUserM(user);
            List<Friend> friend2 = friendRepository.findAllByUserY(user);
        //            groupUsers.stream().map(GroupUser::getUser).toArray(User[]::new);

             Set<User> friends = friend1.stream().map(Friend::getUserY).collect(Collectors.toSet());
            List<User> users2 = friend2.stream().map(Friend::getUserM).collect(Collectors.toList());
            friends.addAll(users2);
//            friends.addAll(friend2.stream().map(Friend::getUserM).collect(Collectors.toSet()));
            friends.remove(user);
            List<JSONObject> data = new ArrayList<>();
            for(User each:friends){
                JSONObject jsonobject = new JSONObject();
                jsonobject.put("nickname", each.getNickname());
                jsonobject.put("avatar", each.getAvatar());
                jsonobject.put("signature", each.getSignature());
                jsonobject.put("id", each.getId());
                jsonobject.put("gender", each.getGender());
                data.add(jsonobject);
            }
            JSONObject res = new JSONObject();
            res.put("code",0);
            res.put("data",data);
            return res;
    }

    public boolean checkFriend(int userYid, int userMid) {
        User userY = userRepository.findById(userYid).get();
        User userM = userRepository.findById(userMid).get();
        return friendRepository.findByUserMAndUserY(userY, userM) != null;
    }

    //fail: -1  success: 0
    public int deleteFriend(User thisUser, int friendId) {
        User friend = userRepository.findById(friendId).get();
        if (friend == null)
            return -1;
        Friend friendRelationship;
        if (thisUser.getId() < friendId)
            friendRelationship = friendRepository.findByUserMAndUserY(thisUser, friend);
        else
            friendRelationship = friendRepository.findByUserMAndUserY(friend, thisUser);
        if (friendRelationship == null)
            return -1;
        else {
            friendRepository.delete(friendRelationship);
            return 0;
        }
    }

    public void addFriend(int userMid, int userYid) {
        User userM = userRepository.findById(userMid).get();
        User userY = userRepository.findById(userYid).get();
        Friend friend = new Friend(userY,userM);
        friendRepository.save(friend);
    }

}
