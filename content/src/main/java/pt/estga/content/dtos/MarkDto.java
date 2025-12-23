package pt.estga.content.dtos;

public record MarkDto(
        Long id,
        String title,
        String description,
        Long coverId
) { }
