package pt.estga.detection.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DetectionResponseDto(
    boolean isMasonMark,
    List<Double> embedding
) {}
