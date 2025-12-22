package pt.estga.proposals.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.user.entities.User;

import java.util.List;
import java.util.Optional;

public interface MarkOccurrenceProposalService {

    Page<MarkOccurrenceProposal> getAll(Pageable pageable);

    Optional<MarkOccurrenceProposal> findById(Long id);
    
    Optional<MarkOccurrenceProposal> findIncompleteByUserId(Long userId);

    List<MarkOccurrenceProposal> findByUser(User user);

    MarkOccurrenceProposal create(MarkOccurrenceProposal proposal);

    MarkOccurrenceProposal update(MarkOccurrenceProposal proposal);

    void delete(MarkOccurrenceProposal proposal);

    long countApprovedProposalsByUser(User user);

}
