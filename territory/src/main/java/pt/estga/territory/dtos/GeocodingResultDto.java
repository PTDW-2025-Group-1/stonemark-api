package pt.estga.territory.dtos;

import lombok.Builder;

@Builder
public record GeocodingResultDto(
    String name,
    String address,
    String city,
    String description,
    String street,
    String houseNumber
) {}
