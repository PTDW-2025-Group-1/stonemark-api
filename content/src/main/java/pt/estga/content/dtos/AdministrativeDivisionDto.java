package pt.estga.content.dtos;

import lombok.Builder;

@Builder
public record AdministrativeDivisionDto(
    Long id,
    String name,
    int adminLevel
) {}
