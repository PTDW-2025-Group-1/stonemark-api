package pt.estga.proposal.mappers;

import org.mapstruct.Mapper;
import pt.estga.file.mappers.MediaFileMapper;
import pt.estga.proposal.dtos.ProposedMarkDto;
import pt.estga.proposal.entities.ProposedMark;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class})
public interface ProposedMarkMapper {
    ProposedMarkDto toDto(ProposedMark entity);
}
