package pt.estga.content.dtos;

import java.time.Instant;

public record MarkOccurrenceDetailedDto(
    Long id,
    Long markId,
    MonumentDto monument,
    Long coverId,
    String proposer,
    Instant createdAt
) { }
