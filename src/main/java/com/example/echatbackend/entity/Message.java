package com.example.echatbackend.entity;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.*;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @OneToOne(fetch=FetchType.EAGER)
    private User userM;  // 为保证同步更新，消息发出方的信息从User表内获取



    @Column(nullable = false)
    private String conversationId;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<User> readList = new HashSet<>();

    @CreatedDate
    private Long time;


    private String message = "";


    private String style = "mess";  // mess 常规消息 emoji 表情包 img 图片 file 文件 ...

    private String state = "friend"; //group friend

    private String type; // validate

    private String status = "0";// 0 未操作 1 同意 2 拒绝
    //对于type为info的消息来说，则是-1拒绝，1同意


    @OneToOne(fetch=FetchType.EAGER)
    private Group group;

    @OneToOne(fetch=FetchType.EAGER)
    private User userY;

    public Message() {
    }

    public Message(User userM, String conversationId, List<User> readList, String message, String style) {
        this.userM = userM;
        this.conversationId=conversationId;
        this.readList=Set.copyOf(readList);
        this.message=message;
        this.style = style;
    }


    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId(){
        return this.id;
    }
    public JSONObject show() {
        List<String> nameList = new ArrayList<>();
        for(User user:readList){
            nameList.add(user.getUserName());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",id);
        jsonObject.put("userM", userM.getId());
        if(userY!=null)
        jsonObject.put("userY", userY.getId());
        if(group!=null)
            jsonObject.put("groupId", group.getId());
        jsonObject.put("mes", message);
        jsonObject.put("state", state);
        jsonObject.put("type", type);
        jsonObject.put("time", time);
        jsonObject.put("avatar", userM.getAvatar());
        jsonObject.put("style", style);
        jsonObject.put("status", status);
        jsonObject.put("read", nameList);
        jsonObject.put("conversationId", conversationId);
        jsonObject.put("name", userM.getUserName());
        jsonObject.put("nickname", userM.getNickname());
        return jsonObject;
    }

}
