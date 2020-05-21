package com.example.echatbackend.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
public class GroupUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @OneToOne
    private Group group;  // 群id

    @OneToOne
    private User user;//群成员

    @Setter
    private Integer isManager;//是否为管理员1是0否

    @Setter
    private Integer isHolder;//是否为群主1是0否

    @Setter
    private String card;//群简介

}
