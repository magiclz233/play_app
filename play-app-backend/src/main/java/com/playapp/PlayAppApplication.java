package com.playapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.playapp.mapper")
@EnableScheduling
public class PlayAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlayAppApplication.class, args);
    }
}
