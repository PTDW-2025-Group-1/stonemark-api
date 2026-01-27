package pt.estga.proposal.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.proposal.dtos.MarkOccurrenceProposalListDto;
import pt.estga.proposal.dtos.MarkOccurrenceProposalStatsDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.user.entities.User;

import java.util.Optional;

public interface MarkOccurrenceProposalService {

    Page<MarkOccurrenceProposal> getAll(Pageable pageable);

    Optional<MarkOccurrenceProposal> findById(Long id);

    Optional<MarkOccurrenceProposal> findDetailedById(Long id);

    Optional<MarkOccurrenceProposal> findWithDetailsById(Long id);
    
    Optional<MarkOccurrenceProposal> findIncompleteByUserId(Long userId);

    Page<MarkOccurrenceProposal> findDetailedByUser(User user, Pageable pageable);

    Page<MarkOccurrenceProposalListDto> findListByUser(User user, Pageable pageable);

    MarkOccurrenceProposal create(MarkOccurrenceProposal proposal);

    MarkOccurrenceProposal update(MarkOccurrenceProposal proposal);

    void delete(MarkOccurrenceProposal proposal);

    MarkOccurrenceProposalStatsDto getStatsByUser(User user);

    long countApprovedProposalsByUserId(Long userId);

}
