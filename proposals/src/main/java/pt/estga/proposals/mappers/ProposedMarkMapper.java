package pt.estga.proposals.mappers;

import org.mapstruct.Mapper;
import pt.estga.file.mappers.MediaFileMapper;
import pt.estga.proposals.dtos.ProposedMarkDto;
import pt.estga.proposals.entities.ProposedMark;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class})
public interface ProposedMarkMapper {
    ProposedMarkDto toDto(ProposedMark entity);
}
