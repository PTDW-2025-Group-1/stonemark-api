package pt.estga.detection.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DetectionRequestDto(
    @JsonProperty("image_data")
    byte[] imageData
) {}
