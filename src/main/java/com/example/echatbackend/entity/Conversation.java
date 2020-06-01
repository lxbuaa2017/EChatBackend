package com.example.echatbackend.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @Setter
    @OneToMany
    private List<User> users;

    @Setter
    private String type;  // group friend

    @OneToOne
    private Group group;
}
