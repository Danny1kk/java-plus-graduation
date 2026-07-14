package ru.practicum.stats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.service.StatsService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class StatsController {

    private final StatsService statsService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto createHit(@RequestBody EndpointHitDto endpointHitDto) {
        statsService.saveHit(endpointHitDto);
        return endpointHitDto;
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {

        String decodedStart = start;
        String decodedEnd = end;

        try {
            decodedStart = URLDecoder.decode(start, StandardCharsets.UTF_8);
            decodedEnd = URLDecoder.decode(end, StandardCharsets.UTF_8);
        } catch (Exception e) {
        }

        LocalDateTime startTime = LocalDateTime.parse(decodedStart, formatter);
        LocalDateTime endTime = LocalDateTime.parse(decodedEnd, formatter);

        return statsService.getStats(startTime, endTime, uris, unique);
    }
}