package ru.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.repository.HitRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MyServiceTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HitRepository hitRepository;

    @BeforeEach
    void setUp() {
        hitRepository.deleteAll();
    }

    @Test
    void shouldCreateHit() throws Exception {
        EndpointHitDto endpointHitDto = new EndpointHitDto(
                "ewm-main-service",
                "/events/1",
                "192.168.0.1",
                LocalDateTime.of(2022, 9, 6, 11, 0, 23));

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endpointHitDto)))
                .andExpect(status().isCreated());

        List<EndpointHit> hits = hitRepository.findAll();
        assertThat(hits).hasSize(1);
        EndpointHit savedHit = hits.get(0);
        assertThat(savedHit.getApp()).isEqualTo("ewm-main-service");
        assertThat(savedHit.getUri()).isEqualTo("/events/1");
        assertThat(savedHit.getIp()).isEqualTo("192.168.0.1");
        assertThat(savedHit.getTimestamp()).isEqualTo(LocalDateTime.of(2022, 9, 6, 11, 0, 23));
    }

    @Test
    void shouldReturnStatsForAllQueryModes() throws Exception {
        hitRepository.saveAll(List.of(
                new EndpointHit(null, "ewm-main-service", "/events/1", "192.168.0.1",
                        LocalDateTime.of(2022, 9, 6, 10, 0, 0)),
                new EndpointHit(null, "ewm-main-service", "/events/1", "192.168.0.1",
                        LocalDateTime.of(2022, 9, 6, 10, 5, 0)),
                new EndpointHit(null, "ewm-main-service", "/events/1", "192.168.0.2",
                        LocalDateTime.of(2022, 9, 6, 10, 10, 0)),
                new EndpointHit(null, "ewm-main-service", "/events/2", "192.168.0.3",
                        LocalDateTime.of(2022, 9, 6, 10, 15, 0))));

        mockMvc.perform(get("/stats")
                        .param("start", "2022-09-06 09:00:00")
                        .param("end", "2022-09-06 11:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").value(3))
                .andExpect(jsonPath("$[1].uri").value("/events/2"))
                .andExpect(jsonPath("$[1].hits").value(1));

        mockMvc.perform(get("/stats")
                        .param("start", "2022-09-06 09:00:00")
                        .param("end", "2022-09-06 11:00:00")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").value(2))
                .andExpect(jsonPath("$[1].uri").value("/events/2"))
                .andExpect(jsonPath("$[1].hits").value(1));

        mockMvc.perform(get("/stats")
                        .param("start", "2022-09-06 09:00:00")
                        .param("end", "2022-09-06 11:00:00")
                        .param("uris", "/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").value(3))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/stats")
                        .param("start", "2022-09-06 09:00:00")
                        .param("end", "2022-09-06 11:00:00")
                        .param("uris", "/events/1")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").value(2))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenStartIsAfterEnd() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2022-09-06 12:00:00")
                        .param("end", "2022-09-06 11:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Некорректный запрос."))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        "Дата начала должна быть раньше или равна дате окончания")));
    }
}