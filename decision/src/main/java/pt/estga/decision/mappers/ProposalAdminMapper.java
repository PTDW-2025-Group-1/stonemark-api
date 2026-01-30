package pt.estga.decision.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pt.estga.decision.dtos.ProposalAdminDetailDto;
import pt.estga.decision.dtos.ProposalAdminListDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

@Mapper(componentModel = "spring")
public interface ProposalAdminMapper {

    @Mapping(target = "title", source = "proposal", qualifiedByName = "generateTitle")
    @Mapping(target = "photoId", source = "originalMediaFile.id")
    @Mapping(target = "submittedByUsername", source = "submittedBy.username")
    ProposalAdminListDto toAdminListDto(MarkOccurrenceProposal proposal);

    @Mapping(target = "photoId", source = "originalMediaFile.id")
    @Mapping(target = "submittedById", source = "submittedBy.id")
    @Mapping(target = "submittedByUsername", source = "submittedBy.username")
    @Mapping(target = "existingMonumentId", source = "existingMonument.id")
    @Mapping(target = "existingMonumentName", source = "existingMonument.name")
    @Mapping(target = "existingMarkId", source = "existingMark.id")
    ProposalAdminDetailDto toAdminDetailDto(MarkOccurrenceProposal proposal);

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
