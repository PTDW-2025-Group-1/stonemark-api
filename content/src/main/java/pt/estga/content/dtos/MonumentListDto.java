package pt.estga.content.dtos;

import pt.estga.administrative.dto.AdministrativeDivisionDto;

public record MonumentListDto(
        Long id,
        Long coverId,
        String name,
        AdministrativeDivisionDto parish
) {
}
