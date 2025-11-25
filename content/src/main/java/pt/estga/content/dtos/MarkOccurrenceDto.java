package pt.estga.content.dtos;

import pt.estga.user.dtos.UserDto;

import java.time.Instant;

public record MarkOccurrenceDto (
    Long id,
    MarkDto mark,
    UserDto user,
    Instant createdAt
) { }
