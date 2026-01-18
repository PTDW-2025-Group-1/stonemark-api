package pt.estga.content.dtos;

import java.time.Instant;

public record MarkOccurrenceListDto(
        Long id,
        Long coverId,
        MonumentMinDto monument,
        Instant publishedAt
) {
}
