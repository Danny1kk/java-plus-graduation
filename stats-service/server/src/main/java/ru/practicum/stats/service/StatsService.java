package ru.practicum.stats.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.exception.BadRequestException;
import ru.practicum.stats.exception.InvalidDateRangeException;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class StatsService {
    private final HitRepository hitRepository;

    public StatsService(HitRepository hitRepository) {
        this.hitRepository = hitRepository;
    }

    @Transactional
    public void saveHit(EndpointHitDto endpointHitDto) {
        validateHit(endpointHitDto);
        hitRepository.save(new EndpointHit(
                null,
                endpointHitDto.app(),
                endpointHitDto.uri(),
                endpointHitDto.ip(),
                endpointHitDto.timestamp()));
    }

    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        validateDateRange(start, end);
        List<String> normalizedUris = normalizeUris(uris);
        if (normalizedUris.isEmpty()) {
            return unique
                    ? hitRepository.findAllStatsWithUniqueIp(start, end)
                    : hitRepository.findAllStats(start, end);
        }
        return unique
                ? hitRepository.findStatsByUrisWithUniqueIp(start, end, normalizedUris)
                : hitRepository.findStatsByUris(start, end, normalizedUris);
    }

    private void validateHit(EndpointHitDto endpointHitDto) {
        if (endpointHitDto == null) {
            throw new BadRequestException("Тело запроса должно быть указано.");
        }
        if (!StringUtils.hasText(endpointHitDto.app())) {
            throw new BadRequestException("Поле app не должно быть пустым.");
        }
        if (!StringUtils.hasText(endpointHitDto.uri())) {
            throw new BadRequestException("Поле uri не должно быть пустым.");
        }
        if (!StringUtils.hasText(endpointHitDto.ip())) {
            throw new BadRequestException("Поле ip не должно быть пустым.");
        }
        if (endpointHitDto.timestamp() == null) {
            throw new BadRequestException("Поле timestamp должно быть указано.");
        }
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Параметры start и end должны быть указаны.");
        }
        if (start.isAfter(end)) {
            throw new InvalidDateRangeException(String.format(
                    "Дата начала должна быть раньше или равна дате окончания. start=%s, end=%s",
                    start,
                    end));
        }
    }

    private List<String> normalizeUris(List<String> uris) {
        if (uris == null) {
            return Collections.emptyList();
        }
        return uris.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }
}