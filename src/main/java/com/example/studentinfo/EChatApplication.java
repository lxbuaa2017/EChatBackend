package com.example.studentinfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(EChatApplication.class, args);
    }

}
