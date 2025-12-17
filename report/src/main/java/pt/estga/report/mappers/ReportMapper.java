package pt.estga.report.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.estga.report.dtos.ReportRequestDto;
import pt.estga.report.dtos.ReportResponseDto;
import pt.estga.report.entities.Report;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(source = "user.id", target = "userId")
    ReportResponseDto toDto(Report report);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Report toEntity(ReportRequestDto dto);
}
