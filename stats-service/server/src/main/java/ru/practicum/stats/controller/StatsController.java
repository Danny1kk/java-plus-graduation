package ru.practicum.stats.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsConstants;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.exception.BadRequestException;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void createHit(@RequestBody EndpointHitDto endpointHitDto) {
        statsService.saveHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
//            @RequestParam @DateTimeFormat(pattern = StatsConstants.DATE_TIME_PATTERN) LocalDateTime start,
//            @RequestParam @DateTimeFormat(pattern = StatsConstants.DATE_TIME_PATTERN) LocalDateTime end,
//            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false) @DateTimeFormat(pattern = StatsConstants.DATE_TIME_PATTERN) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = StatsConstants.DATE_TIME_PATTERN) LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {

        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Дата начала не может быть позже даты конца");
        }

        return statsService.getStats(start, end, uris, unique);
    }
}