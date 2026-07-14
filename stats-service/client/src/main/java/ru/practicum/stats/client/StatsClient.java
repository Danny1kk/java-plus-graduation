//package ru.practicum.stats.client;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.client.discovery.DiscoveryClient;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//import ru.practicum.stats.dto.EndpointHitDto;
//import ru.practicum.stats.dto.StatsConstants;
//import ru.practicum.stats.dto.ViewStatsDto;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Slf4j
//@Service
//public class StatsClient {
//
//    private final RestTemplate restTemplate;
//    private final DiscoveryClient discoveryClient;
//    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(StatsConstants.DATE_TIME_PATTERN);
//
//    public StatsClient(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
//        this.restTemplate = restTemplate;
//        this.discoveryClient = discoveryClient;
//    }
//
//    private String getBaseUrl() {
//        return discoveryClient.getInstances("stats-server")
//                .stream()
//                .findFirst()
//                .map(instance -> "http://" + instance.getHost() + ":" + instance.getPort())
//                .orElse("http://localhost:9090");
//    }
//
//    public void hit(EndpointHitDto hitDto) {
//        try {
//            restTemplate.postForEntity(getBaseUrl() + "/hit", hitDto, Object.class);
//            log.info("Статистика успешно отправлена для URI: {}", hitDto.getUri());
//        } catch (Exception e) {
//            log.error("Ошибка при отправке hit: {}", e.getMessage());
//        }
//    }
//
////    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
////        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUrl() + "/stats")
////                .queryParam("start", start.format(FORMATTER))
////                .queryParam("end", end.format(FORMATTER))
////                .queryParam("unique", unique);
////
////        if (uris != null && !uris.isEmpty()) {
////            builder.queryParam("uris", uris.toArray());
////        }
////
////        try {
////            ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(
////                    builder.build().toUri(),
////                    ViewStatsDto[].class
////            );
////
////            if (response.getBody() != null) {
////                return Arrays.asList(response.getBody());
////            }
////        } catch (Exception e) {
////            log.error("Ошибка при получении статистики с сервера: {}", e.getMessage());
////        }
////
////        return List.of();
////    }
//
//    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("start", start.format(FORMATTER));
//        parameters.put("end", end.format(FORMATTER));
//        parameters.put("unique", unique);
//
//        String url = getBaseUrl() + "/stats?start={start}&end={end}&unique={unique}";
//
//        if (uris != null && !uris.isEmpty()) {
//            parameters.put("uris", String.join(",", uris));
//            url += "&uris={uris}";
//        }
//
//        try {
//            ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(
//                    url,
//                    ViewStatsDto[].class,
//                    parameters
//            );
//
//            if (response.getBody() != null) {
//                return Arrays.asList(response.getBody());
//            }
//        } catch (Exception e) {
//            log.error("Ошибка при получении статистики с сервера: {}", e.getMessage());
//        }
//
//        return List.of();
//    }
//}

package ru.practicum.stats.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.util.List;

@FeignClient(name = "stats-service", fallback = StatsFeignClientFallback.class)
public interface StatsClient {

    @PostMapping("/hit")
    void hit(@RequestBody EndpointHitDto hitDto);

    @GetMapping("/stats")
    List<ViewStatsDto> getStats(@RequestParam("start") String start,
                                @RequestParam("end") String end,
                                @RequestParam(value = "uris", required = false) List<String> uris,
                                @RequestParam(value = "unique", defaultValue = "false") boolean unique);
}
//    private final RestTemplate restTemplate;
//    private final DiscoveryClient discoveryClient;
//    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(StatsConstants.DATE_TIME_PATTERN);
//
//    public StatsClient(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
//        this.restTemplate = restTemplate;
//        this.discoveryClient = discoveryClient;
//    }
//
//    private String getBaseUrl() {
//        return discoveryClient.getInstances("stats-service")
//                .stream()
//                .findFirst()
//                .map(instance -> "http://" + instance.getHost() + ":" + instance.getPort())
//                .orElse("http://localhost:9090");
//    }
//
//    public void hit(EndpointHitDto hitDto) {
//        try {
//            restTemplate.postForEntity(getBaseUrl() + "/hit", hitDto, Object.class);
//            log.info("Статистика успешно отправлена для URI: {}", hitDto.getUri());
//        } catch (Exception e) {
//            log.error("Ошибка при отправке hit: {}", e.getMessage());
//        }
//    }
//
//    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUrl() + "/stats")
//                .queryParam("start", start.format(FORMATTER))
//                .queryParam("end", end.format(FORMATTER))
//                .queryParam("unique", unique);
//
//        if (uris != null && !uris.isEmpty()) {
//            for (String uri : uris) {
//                builder.queryParam("uris", uri);
//            }
//        }
//
//        try {
//            ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(
//                    builder.build().encode().toUri(),
//                    ViewStatsDto[].class
//            );
//
//            if (response.getBody() != null) {
//                return Arrays.asList(response.getBody());
//            }
//        } catch (Exception e) {
//            log.error("Ошибка при получении статистики с сервера: {}", e.getMessage());
//        }
//
//        return List.of();
//    }
//}