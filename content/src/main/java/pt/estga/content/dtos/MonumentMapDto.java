package pt.estga.content.dtos;

public record MonumentMapDto(
        Long id,
        String name,
        Double latitude,
        Double longitude
) { }