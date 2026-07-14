package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.util.Collections;
import java.util.List;

@Slf4j
public class StatsFeignClientFallback implements StatsClient {

    @Override
    public void hit(EndpointHitDto hitDto) {
        log.warn("Не удалось отправить статистику: {}", hitDto);
    }

    @Override
    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        log.warn("Не удалось получить статистику, возвращаем пустой список");
        return Collections.emptyList();
    }
}