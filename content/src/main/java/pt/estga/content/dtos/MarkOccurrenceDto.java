package pt.estga.content.dtos;

public record MarkOccurrenceDto (
    Long id,
    Long coverId,
    MarkDto mark,
    MonumentDto monument
) { }
