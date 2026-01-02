package pt.estga.content.dtos;

import lombok.Builder;

@Builder
public record AdministrativeDivisionDto(
    String district,
    String municipality,
    String parish
) {}
