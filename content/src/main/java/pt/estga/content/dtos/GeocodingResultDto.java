package pt.estga.content.dtos;

import lombok.Builder;

@Builder
public record GeocodingResultDto(
    String name,
    String address,
    String city,
    String description
) {}
