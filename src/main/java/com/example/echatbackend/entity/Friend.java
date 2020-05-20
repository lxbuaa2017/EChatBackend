package com.example.echatbackend.entity;

import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

public class Friend {//
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @Setter
    private User userY;

    @Setter
    private User userM;

    @CreatedDate
    @Setter
    private Date createDate;
}
