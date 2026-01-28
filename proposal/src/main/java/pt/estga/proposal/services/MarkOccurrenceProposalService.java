package pt.estga.proposal.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.projections.MarkOccurrenceProposalStatsProjection;
import pt.estga.user.entities.User;

import java.util.Optional;

public interface MarkOccurrenceProposalService {

    Page<MarkOccurrenceProposal> getAll(Pageable pageable);

    Optional<MarkOccurrenceProposal> findById(Long id);

    Page<MarkOccurrenceProposal> findByUser(User user, Pageable pageable);

    Optional<MarkOccurrenceProposal> findByIdWithRelations(Long id);

    MarkOccurrenceProposalStatsProjection getStatsByUser(User user);

    MarkOccurrenceProposal create(MarkOccurrenceProposal proposal);

    MarkOccurrenceProposal update(MarkOccurrenceProposal proposal);

    void delete(MarkOccurrenceProposal proposal);

}
