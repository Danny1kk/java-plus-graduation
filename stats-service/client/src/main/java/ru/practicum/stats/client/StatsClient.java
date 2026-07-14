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
import java.util.Objects;

@Slf4j
@Service
public class StatsClient {

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(StatsConstants.DATE_TIME_PATTERN);

    public StatsClient(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    private String getBaseUrl() {
        return discoveryClient.getInstances("stats-server")
                .stream()
                .findFirst()
                .map(instance -> "http://" + instance.getHost() + ":" + instance.getPort())
                .orElse("http://localhost:9090");
    }

    public void hit(EndpointHitDto hitDto) {
        try {
            restTemplate.postForEntity(getBaseUrl() + "/hit", hitDto, Object.class);
        } catch (Exception e) {
            log.error("Ошибка при отправке hit: {}", e.getMessage());
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUrl() + "/stats")
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER));

            if (uris != null) {
                for (String uri : uris) {
                    builder.queryParam("uris", uri);
                }
            }

            if (unique != null) {
                builder.queryParam("unique", unique);
            }

            String url = builder.build(false).toUriString();
            ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(url, ViewStatsDto[].class);
            return Arrays.asList(Objects.requireNonNull(response.getBody()));
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}