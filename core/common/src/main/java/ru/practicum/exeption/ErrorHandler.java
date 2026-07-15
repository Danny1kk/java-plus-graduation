package ru.practicum.exeption;

import feign.FeignException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException e) {
        return Map.of(
                "status", "NOT_FOUND",
                "reason", "The required object was not found.",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(RuntimeException e) {
        return Map.of(
                "status", "BAD_REQUEST",
                "reason", "Incorrectly made request.",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getFieldError() != null
                ? e.getFieldError().getDefaultMessage()
                : "Ошибка валидации";
        return Map.of(
                "status", "BAD_REQUEST",
                "reason", "Incorrectly made request.",
                "message", message,
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            DateTimeParseException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleRequestParsing(Exception e) {
        return Map.of(
                "status", "BAD_REQUEST",
                "reason", "Incorrectly made request.",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflict(ConflictException e) {
        return Map.of(
                "status", "CONFLICT",
                "reason", "Integrity constraint has been violated.",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDataIntegrity(DataIntegrityViolationException e) {
        return Map.of(
                "status", "CONFLICT",
                "reason", "Integrity constraint has been violated.",
                "message", e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : "Нарушение целостности данных",
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> handleFeignException(FeignException e) {
        HttpStatus status = HttpStatus.valueOf(e.status());
        Map<String, String> body = Map.of(
                "status", status.name(),
                "reason", status == HttpStatus.CONFLICT ? "Integrity constraint has been violated." : "Internal server error.",
                "message", e.contentUTF8(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGeneral(Exception e) {
        return Map.of(
                "status", "INTERNAL_SERVER_ERROR",
                "reason", "Internal server error.",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }
}