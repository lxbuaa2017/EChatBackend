package com.example.echatbackend.entity;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @OneToOne
    private User userM;  // 为保证同步更新，消息发出方的信息从User表内获取



    @Column(nullable = false)
    private String conversationId;

    @OneToMany
    private List<User> readList;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;

    private String message;


    private String style;  // mess 常规消息 emoji 表情包 img 图片 file 文件 ...

    private String state; //group friend

    private String type; // validate

    private String status;// 0 未操作 1 同意 2 拒绝


    @OneToOne
    private Group group;

    @OneToOne
    private User userY;

    public Message() {
    }

    public Message(User userM, String conversationId, List<User> readList, String message, String style) {
        this.userM = userM;
        this.conversationId=conversationId;
        this.readList=readList;
        this.message=message;
        this.style = style;
    }

    public JSONObject show() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("mes", message);
        jsonObject.put("time", time);
        jsonObject.put("style", style);
        jsonObject.put("read", readList);
        jsonObject.put("userM", userM);
//        jsonObject.put("userId", user.id);
//        jsonObject.put("nickname", user.getUserName());
//        jsonObject.put("avatar", user.getAvatar());
        return jsonObject;
    }

}
