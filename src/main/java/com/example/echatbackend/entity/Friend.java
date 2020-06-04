package com.example.echatbackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    public Friend() {
    }

    public Friend(User userY,User userM) {
        this.userY=userY;
        this.userM=userM;
    }
}
