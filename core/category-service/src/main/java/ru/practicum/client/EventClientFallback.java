package ru.practicum.client;

import org.springframework.stereotype.Component;

@Component
public class EventClientFallback implements EventClient {
    @Override
    public Boolean existsByCategoryId(Long categoryId) {
        throw new RuntimeException("Служба обработки событий недоступна.");
    }
}