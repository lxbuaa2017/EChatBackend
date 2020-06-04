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
            if (friend == null || user == null)
                return null;
            jsonobject.put("createDate", friend.getCreateDate());
            jsonobject.put("nickname", user.getNickname());
            jsonobject.put("avatar", user.getAvatar());
            jsonobject.put("signature", user);
            jsonobject.put("id", user.getId());
            jsonobject.put("gender", user.getGender());
            return jsonobject;
        } else
            return null;
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
}
