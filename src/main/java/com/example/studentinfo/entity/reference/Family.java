package com.example.studentinfo.entity.reference;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table
@Getter
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column
    @Setter
    private String salary;

    @Column(columnDefinition = "text")
    @Setter
    private String info;
}
