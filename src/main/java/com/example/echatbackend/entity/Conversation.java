package com.example.echatbackend.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @Setter
    @OneToOne
    private User user;

    @Setter
    @OneToOne
    private Group group;

    @Setter
    private String type;  // group friend

}
