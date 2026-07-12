package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(RestTemplate restTemplate, @Value("${stats-server.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public void hit(EndpointHitDto hitDto) {
        restTemplate.postForEntity(baseUrl + "/hit", hitDto, Object.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String url = baseUrl + "/stats"
                + "?start=" + URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8)
                + "&end=" + URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8);

        if (uris != null && !uris.isEmpty()) {
            url += "&uris=" + URLEncoder.encode(String.join(",", uris), StandardCharsets.UTF_8);
        }
        if (unique != null) {
            url += "&unique=" + unique;
        }

        ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(url, ViewStatsDto[].class);
        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }
}