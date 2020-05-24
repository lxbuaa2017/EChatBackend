package com.example.echatbackend.entity;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;

    @Column(nullable = false, unique = true)
    @Setter
    protected String userName;

    @Setter
    private String nickname;

    @OneToMany(cascade = CascadeType.REMOVE)
    @Setter
    private List<Cover> cover = new ArrayList<>();

    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Setter
    private Integer gender;  // 未知 0 男 1 女 2

    @CreatedDate
    private Date signUpTime;

    @CreatedDate
    @Setter
    private Date lastLoginTime;

    @Setter
    private String bubble = "vchat";

    @Setter
    private String photo = "/img/picture.png";

    @Setter
    private String chatColor = "#ffffff";

    @Column(nullable = false)
    @Setter
    private Double bgOpa = 0.2;

    @Setter
    private String wallpaper = "/img/wallpaper.jpg";

    @Column(nullable = false, columnDefinition = "char(64)")
    private String pswSHA256;

    @Column(nullable = false, columnDefinition = "char(32)")
    private String salt;

    @Setter
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<Conversation> conversationList = new ArrayList<>();

    private static String shaSaltSha(String password, String salt) {
        return new Sha256Hash(new Sha256Hash(password).toHex(), salt).toHex();
    }

    public void setPassword(String password) {
        pswSHA256 = shaSaltSha(password, salt);
    }

    public void randomSalt() {
        salt = RandomStringUtils.randomAlphanumeric(32);
    }

    public boolean checkPassword(String password) {
        return pswSHA256.equals(shaSaltSha(password, salt));
    }

    @Nullable
    @Contract(pure = true)
    public static String emailFormat(String email) {
        if (email == null || !email.matches("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+")) {
            return null;
        }
        return email.toLowerCase();
    }
}