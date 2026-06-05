package com.playapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.playapp.mapper")
public class PlayAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlayAppApplication.class, args);
    }
}
