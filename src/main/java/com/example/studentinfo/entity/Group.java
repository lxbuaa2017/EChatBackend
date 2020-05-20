package com.example.studentinfo.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @Column(nullable = false)
    @Setter
    private String title;

    @Setter
    private String description;

    @Setter
    private String photo;

    @Column(nullable = false, unique = true)
    private Integer code;  // 群账号

    @Column(nullable = false)
    private Integer userNum;  // 群成员数量，避免某些情况需要多次联表查找，如搜索；所以每次加入一人，数量加一

    @OneToOne
    private User user;  // 群主账号
}
