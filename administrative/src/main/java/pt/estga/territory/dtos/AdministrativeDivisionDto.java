package pt.estga.territory.dtos;

import lombok.Builder;
import pt.estga.territory.entities.LogicalLevel;
import pt.estga.territory.entities.OsmType;

@Builder
public record AdministrativeDivisionDto(
    Long osmId,
    OsmType osmType,
    Integer osmAdminLevel,
    String name,
    String namePt,
    LogicalLevel logicalLevel,
    Long parentId
) {}
