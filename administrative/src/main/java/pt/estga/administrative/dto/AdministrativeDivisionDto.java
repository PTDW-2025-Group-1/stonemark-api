package pt.estga.administrative.dto;

import lombok.Builder;

@Builder
public record AdministrativeDivisionDto(
    Long osmId,
    String name,
    int adminLevel
) {}
