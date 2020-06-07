package com.example.echatbackend.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "tb_group")
public class Group {

    @Column(nullable = false)
    @Setter
    private String name;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;
    @Setter
    private String avatar;

    @OneToOne
    private Conversation conversation;

    @Setter
    private String description;
    @Column(nullable = false)
    @Setter
    private Integer userNum;  // 群成员数量，避免某些情况需要多次联表查找，如搜索；所以每次加入一人，数量加一

    @Column(nullable = false, unique = true)
    private Integer code;  // 群账号

    @OneToOne
    @Setter
    private User user;  // 群主账号

    public Group() {
        this.userNum = 1;
    }

    public JSONObject show() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("groupName", name);
        jsonObject.put("groupDesc", description);
        jsonObject.put("groupAvatar", avatar);
        jsonObject.put("holder", user.id);
        return jsonObject;
    }
}
