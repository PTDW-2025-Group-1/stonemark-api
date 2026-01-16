package pt.estga.territory.dtos;

import lombok.Builder;

@Builder
public record AdministrativeDivisionDto(
    Long id,
    Integer osmAdminLevel,
    String name,
    Long parentId,
    Integer monumentsCount
) {}
