package ru.practicum.client;

import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public Boolean existsById(Long userId) {
        return false;
    }

    @Override
    public String getUserName(Long userId) {
        return null;
    }
}