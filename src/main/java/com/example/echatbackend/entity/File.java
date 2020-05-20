package com.example.echatbackend.entity;

import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.Date;

public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @Setter
    private String url;

    @OneToOne
    private User uploader;  // 群主账号

    @CreatedDate
    @Setter
    private Date uploadDate;
}