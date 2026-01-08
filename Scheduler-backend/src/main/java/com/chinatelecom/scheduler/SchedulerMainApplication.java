package com.chinatelecom.scheduler;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.chinatelecom.scheduler.mapper")
@SpringBootApplication
public class SchedulerMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchedulerMainApplication.class, args);
    }
}