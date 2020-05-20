package com.example.studentinfo.entity.reference;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table
@Getter
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(name = "student_rank")
    @Setter
    private String rank;

    @Column(columnDefinition = "text")
    @Setter
    private String info;
}
