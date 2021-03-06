package com.example.echatbackend.entity;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.*;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@Transactional
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

    private String emoji; //文件url

    private String style = "mess";  // mess 常规消息 emoji 表情包 img 图片 file 文件 ...

    private String state = "friend"; //group friend

    private String type; // validate

    private String status = "0";// 0 未操作 1 同意 2 拒绝
    //对于type为info的消息来说，则是-1拒绝，1同意


    @ManyToOne(fetch=FetchType.EAGER)
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
        User userM = this.getUserM();
        jsonObject.put("userM", userM.getId());
        jsonObject.put("avatar", userM.getAvatar());
        jsonObject.put("name", userM.getUserName());
        jsonObject.put("nickname", userM.getNickname());
        if(userY!=null)
        jsonObject.put("userY", userY.getId());
        if(group!=null)
            jsonObject.put("groupId", group.getId());
        if(group!=null)
            jsonObject.put("groupName", group.getName());
        if(group!=null)
            jsonObject.put("groupPhoto", group.getAvatar());
        jsonObject.put("mes", message);
        jsonObject.put("state", state);
        jsonObject.put("type", type);
        jsonObject.put("time", time);
        jsonObject.put("emoji", emoji);
        jsonObject.put("style", style);
        jsonObject.put("status", status);
        jsonObject.put("read", nameList);
        jsonObject.put("conversationId", conversationId);
        return jsonObject;
    }
}
