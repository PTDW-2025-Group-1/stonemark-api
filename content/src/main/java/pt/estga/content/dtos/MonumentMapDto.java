package pt.estga.content.dtos;

import pt.estga.territory.dtos.AdministrativeDivisionDto;

public record MonumentMapDto(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        AdministrativeDivisionDto parish
) { }