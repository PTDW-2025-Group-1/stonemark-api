package pt.estga.stonemark.dtos.content;

public record MonumentDto(
        Long id,
        String name,
        String description,
        Double latitude,
        Double longitude
) { }
