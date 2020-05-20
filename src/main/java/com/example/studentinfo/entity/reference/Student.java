package com.example.studentinfo.entity.reference;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Getter
@Setter
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)

    private String name;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false, unique = true)
    private String idNumber;

    @Column(nullable = false)
    private String schoolType;

    @Column(nullable = false)
    private String familyType;

    @Column(nullable = false, columnDefinition = "text")
    private String info;

    @OneToOne
    private County county;

    private String school;

    private String address;

    private String portrait;

    private String monthlyMoney;

    private String homeContact;

    private String homeContact1;

    private String schoolContact;

    private String schoolContact1;

    private Long aidTime;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Family> familyList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Score> scoreList = new ArrayList<>();

    public JSONObject detail() {
        return (JSONObject) JSON.toJSON(this);
    }
}
