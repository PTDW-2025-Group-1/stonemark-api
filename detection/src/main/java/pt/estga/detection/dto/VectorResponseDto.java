package pt.estga.detection.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record VectorResponseDto(
    @JsonProperty("embedding")
    List<Double> vector
) {}
