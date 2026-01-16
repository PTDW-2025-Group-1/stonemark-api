package pt.estga.content.dtos;

import java.time.Instant;

public record MarkOccurrenceDto(
    Long id,
    Long markId,
    MarkDto mark,
    MonumentDto monument,
    Long coverId,
    Long authorId,
    String authorName,
    Instant publishedAt
) { }
