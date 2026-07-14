package ru.practicum.stats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneralError(Exception exception) {
        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "Внутренняя ошибка сервера.",
                exception.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler({BadRequestException.class, InvalidDateRangeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(RuntimeException exception) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.name(),
                "Некорректный запрос.",
                exception.getMessage(),
                LocalDateTime.now());
    }
}