package com.team69.itproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.team69.itproject.mappers")
public class ItProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItProjectApplication.class, args);
    }

}
