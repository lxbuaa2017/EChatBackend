package com.example.echatbackend.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    protected String conversationId;


    @ManyToMany
    private List<User> users = new ArrayList<>();


    private String type;  // group friend

    @OneToOne
    private Group group;

    public JSONObject show(int userId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("type", type);
        if (type.equals("group")) {
            jsonObject.put("name", group.getName());
            jsonObject.put("avatar", group.getAvatar());
            jsonObject.put("itemId", group.getId());
        } else if (type.equals("friend")) {
            for (User user : users) {
                if (user.getId() != userId) {
                    jsonObject.put("name", user.getNickname());
                    jsonObject.put("avatar", user.getAvatar());
                    jsonObject.put("itemId", user.getId());
                    break;
                }
            }
        }
        return jsonObject;
    }
}
