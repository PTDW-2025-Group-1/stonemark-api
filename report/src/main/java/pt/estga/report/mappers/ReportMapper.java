package pt.estga.report.mappers;

import org.mapstruct.Mapper;
import pt.estga.report.dtos.ReportRequestDto;
import pt.estga.report.dtos.ReportResponseDto;
import pt.estga.report.entities.Report;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    ReportResponseDto toDto(Report report);

    Report toEntity(ReportRequestDto dto);
}
