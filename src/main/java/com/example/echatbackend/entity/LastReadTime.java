package com.example.echatbackend.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class LastReadTime {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;
    //不做联表，加快查询
    protected String conversationId;
    protected Integer userId;
    protected Long lastReadTime;
}
