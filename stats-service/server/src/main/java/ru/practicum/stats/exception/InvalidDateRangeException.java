package ru.practicum.stats.exception;

public class InvalidDateRangeException extends BadRequestException {
    public InvalidDateRangeException(String message) {
        super(message);
    }
}