package com.example.echatbackend.service;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.dao.FriendRepository;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.Friend;
import com.example.echatbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        //1.看看这个人是不是好友
        //2.如果是，去user里面把他找出来

        if (friendRepository.existsById(userId)) {
            JSONObject jsonobject = new JSONObject();
            Friend friend = friendRepository.findById(userId).get();
            User user = userRepository.findById(userId).get();
            jsonobject.put("createDate", friend.getCreateDate());
            jsonobject.put("nickname", user.getNickname());
            jsonobject.put("avatar", user.getavatar());
            jsonobject.put("signature", user);
            jsonobject.put("id", user.getId());
            return jsonobject;
        } else
            return null;
    }

    public boolean checkFriend(int userMid, int userYid) {
        User userM = userRepository.findById(userMid).get();
        User userY = userRepository.findById(userYid).get();
        return friendRepository.findByUserMAndUserY(userM, userY) != null || friendRepository.findByUserMAndUserY(userY, userM) != null;
    }
}
