package pt.estga.content.dtos;

import pt.estga.territory.dtos.AdministrativeDivisionDto;

public record MonumentListDto(
        Long id,
        Long coverId,
        String name,
        AdministrativeDivisionDto parish
) {
}
