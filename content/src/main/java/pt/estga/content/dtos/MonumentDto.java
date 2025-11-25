package pt.estga.content.dtos;

public record MonumentDto(
        Long id,
        String name,
        String description,
        Double latitude,
        Double longitude
) { }
