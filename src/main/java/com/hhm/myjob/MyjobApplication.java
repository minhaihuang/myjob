package com.hhm.myjob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableScheduling // 开启定时任务功能
public class MyjobApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyjobApplication.class, args);
    }

}
