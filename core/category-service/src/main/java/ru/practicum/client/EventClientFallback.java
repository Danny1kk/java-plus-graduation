package ru.practicum.client;

import org.springframework.stereotype.Component;

@Component
public class EventClientFallback implements EventClient {
    @Override
    public Boolean existsByCategoryId(Long categoryId) {
        return false;
    }
}