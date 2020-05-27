package com.example.echatbackend.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @OneToOne
    private User userY;  // 由于是多对多关系故单独拿出一个表所以是两个user，userY代表userid较小的

    @OneToOne
    private User userM;//userM代表userid较大的

    @CreatedDate
    @Setter
    private Long createDate;

    public String getCreateDate() {
        return createDate.toString();
    }

}
