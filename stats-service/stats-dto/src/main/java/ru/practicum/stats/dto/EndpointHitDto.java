package ru.practicum.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Builder
public record EndpointHitDto(

    @NotBlank String app,
    @NotBlank String uri,
    @NotBlank String ip,

    @JsonFormat(pattern = StatsConstants.DATE_TIME_PATTERN) LocalDateTime timestamp
) {
}