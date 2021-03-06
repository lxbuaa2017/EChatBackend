package com.example.echatbackend.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @Setter
    private String url;  // 调用的url

    @OneToOne
    private User uploader;  // 上传者

    @CreatedDate
    @Setter
    private Long uploadDate;  // 上传时间
}
