package com.example.echatbackend.entity.reference;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table
@Getter
@Setter
public class County {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String county;

    public County() {
    }

    public County(String county) {
        this.county = county;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        County county1 = (County) o;
        return county.equals(county1.county);
    }

    @Override
    public int hashCode() {
        return Objects.hash(county);
    }
}
