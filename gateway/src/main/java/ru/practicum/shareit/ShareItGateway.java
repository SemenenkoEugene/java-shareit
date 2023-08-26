package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:applicationGateway.properties")
public class ShareItGateway {
    public static void main(String[] args) {
        SpringApplication.run(ShareItGateway.class, args);
    }
}