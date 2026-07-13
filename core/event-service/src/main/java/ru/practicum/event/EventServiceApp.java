package ru.practicum.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"ru.practicum.event", "ru.practicum.stats", "ru.practicum.exeption"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"ru.practicum.category", "ru.practicum.user", "ru.practicum.request"})
public class EventServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApp.class, args);
    }
}