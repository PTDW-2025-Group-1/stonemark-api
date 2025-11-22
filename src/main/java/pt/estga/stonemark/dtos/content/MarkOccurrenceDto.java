package pt.estga.stonemark.dtos.content;

import pt.estga.stonemark.dtos.user.UserDto;

import java.time.Instant;

public record MarkOccurrenceDto (
    Long id,
    MarkDto mark,
    UserDto user,
    Instant createdAt
) { }
