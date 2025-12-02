package pt.estga.detection.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DetectionResponseDto(
    @JsonProperty("is_mason_mark")
    boolean isMasonMark,
    List<Double> embedding
) {}
