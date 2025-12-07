package pt.estga.content.dtos;

import java.time.Instant;

public record MarkOccurrenceDto (
    Long id,
    MarkDto mark,
    MonumentDto monument,
    Instant createdAt,
    String createdBy
) { }
