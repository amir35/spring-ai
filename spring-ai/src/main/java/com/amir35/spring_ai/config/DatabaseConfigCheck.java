package com.amir35.spring_ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class DatabaseConfigCheck {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @PostConstruct
    public void check() {
        System.out.println("================================");
        System.out.println("DB URL      : " + url);
        System.out.println("DB USERNAME : " + username);
        System.out.println("DB PASSWORD : [" + password + "]");
        System.out.println("================================");
    }
}