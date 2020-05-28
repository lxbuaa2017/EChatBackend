package com.example.echatbackend.entity;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;


    private String userName;  // 为保证同步更新，消息发出方的信息从User表内获取


    @Column(nullable = false)
    private Integer conversationId;

    @OneToMany
    private List<User> readList;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;

    private String message;


    private String messageType;  // mess 常规消息 emoji 表情包 img 图片 file 文件 ...

//    private Integer chatType;  // 0单聊 1群聊


    public Message() {
    }

    public Message(String userName, Integer conversationId, List<User> readList, String message, String messageType) {
        this.userName=userName;
        this.conversationId=conversationId;
        this.readList=readList;
        this.message=message;
        this.messageType=messageType;
    }

    public JSONObject show() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("mes", message);
        jsonObject.put("time", time);
        jsonObject.put("style", messageType);
        jsonObject.put("read", readList);
        jsonObject.put("userName", userName);
//        jsonObject.put("userId", user.id);
//        jsonObject.put("nickname", user.getUserName());
//        jsonObject.put("avatar", user.getAvatar());
        return jsonObject;
    }

}
