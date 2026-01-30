package pt.estga.decision.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.decision.dtos.ProposalAdminDetailDto;
import pt.estga.decision.mappers.ProposalAdminMapper;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.shared.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ProposalAdminService {

    private final MarkOccurrenceProposalRepository proposalRepo;
    private final ProposalAdminMapper proposalAdminMapper;

    @Transactional(readOnly = true)
    public ProposalAdminDetailDto getProposalDetails(Long proposalId) {
        // Fetch proposal with eager relations (defined in repository)
        MarkOccurrenceProposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + proposalId));

        return proposalAdminMapper.toAdminDetailDto(proposal);
    }
}
