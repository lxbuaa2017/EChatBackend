package com.example.echatbackend.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Friend {//
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @OneToOne
    private User userY;

    @OneToOne
    private User userM;

    @CreatedDate
    @Setter
    private Date createDate;
}
