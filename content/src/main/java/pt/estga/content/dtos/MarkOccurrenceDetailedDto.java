package pt.estga.content.dtos;

import java.time.Instant;

public record MarkOccurrenceDetailedDto(
    Long id,
    Long markId,
    MonumentDto monument,
    Long coverId,
    Long authorId,
    String authorName,
    Instant publishedAt
) { }
