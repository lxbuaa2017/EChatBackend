package com.example.echatbackend.entity;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    private String name;

    private String photo;

    private Integer type;  // group: 0 friend: 1
}
