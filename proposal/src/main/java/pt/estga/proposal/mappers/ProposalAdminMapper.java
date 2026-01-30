package pt.estga.proposal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pt.estga.proposal.dtos.ProposalAdminListDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

@Mapper(componentModel = "spring")
public interface ProposalAdminMapper {

    @Mapping(target = "title", source = "proposal", qualifiedByName = "generateTitle")
    @Mapping(target = "photoId", source = "originalMediaFile.id")
    @Mapping(target = "submittedByUsername", source = "submittedBy.username")
    ProposalAdminListDto toAdminListDto(MarkOccurrenceProposal proposal);

    @Named("generateTitle")
    default String generateTitle(MarkOccurrenceProposal proposal) {
        if (proposal.getMonumentName() != null) {
            return proposal.getMonumentName();
        } else if (proposal.getExistingMonument() != null) {
            return proposal.getExistingMonument().getName();
        } else {
            return "Proposal #" + proposal.getId();
        }
    }
}
