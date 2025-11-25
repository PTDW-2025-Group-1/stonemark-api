package pt.estga.shared.dtos.content;

import pt.estga.shared.dtos.user.UserDto;

import java.time.Instant;

public record MarkOccurrenceDto (
    Long id,
    MarkDto mark,
    UserDto user,
    Instant createdAt
) { }
