package com.example.echatbackend.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @Setter
    @OneToOne
    private User user;  // 为保证同步更新，消息发出方的信息从User表内获取

    @Setter
    @Column(nullable = false)
    private Integer conversationId;

    @OneToMany
    private final List<User> readList = new ArrayList<>();

    @CreatedDate
    private Long time;

    @Setter
    private String message;

    @Setter
    private String messageType;  // mess 常规消息 emoji 表情包 img 图片 file 文件 ...

    @Setter
    private Integer chatType;  // 0单聊 1群聊

    public JSONObject show() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("mes", message);
        jsonObject.put("time", time);
        jsonObject.put("style", messageType);
        jsonObject.put("read", readList);
        jsonObject.put("name", user.userName);
        jsonObject.put("userId", user.id);
        jsonObject.put("nickname", user.getUserName());
        jsonObject.put("avatar", user.getAvatar());
        return jsonObject;
    }

}
