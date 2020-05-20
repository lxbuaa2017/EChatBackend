package com.example.echatbackend.entity;

import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

public class GroupUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @OneToOne
    private Group group;  // 群id

    @Setter
    private User user;//群成员

    @Setter
    private Integer isManager;//是否为管理员1是0否

    @Setter
    private Integer isHolder;//是否为群主1是0否

    @Setter
    private String card;//群简介

}
