package com.okits02.SpringJWTWithOauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SpringJwtWithOauth2Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringJwtWithOauth2Application.class, args);
    }
}
