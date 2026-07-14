package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.StatsConstants;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class StatsClient {

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(StatsConstants.DATE_TIME_PATTERN);

    public StatsClient(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    private String getBaseUrl() {
        return discoveryClient.getInstances("stats-service")
                .stream()
                .findFirst()
                .map(instance -> "http://" + instance.getHost() + ":" + instance.getPort())
                .orElse("http://stats-service:9090");
    }

    public void hit(EndpointHitDto hitDto) {
        try {
            restTemplate.postForEntity(getBaseUrl() + "/hit", hitDto, Object.class);
            log.info("Статистика отправлена для URI: {}", hitDto.uri());
        } catch (Exception e) {
            log.error("Не удалось отправить hit: app={}, uri={}", hitDto.app(), hitDto.uri(), e);
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUrl() + "/stats")
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER));

            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", uris.toArray());
            }

            if (unique != null) {
                builder.queryParam("unique", unique);
            }

            ResponseEntity<ViewStatsDto[]> response =
                    restTemplate.getForEntity(builder.build().toUri(), ViewStatsDto[].class);

            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }

        } catch (Exception e) {
            log.error("Не удалось получить статистику просмотров", e);
        }

        return Collections.emptyList();
    }
}