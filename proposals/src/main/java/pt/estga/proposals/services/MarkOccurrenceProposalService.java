package pt.estga.proposals.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.user.entities.User;

import java.util.List;
import java.util.Optional;

public interface MarkOccurrenceProposalService {

    Page<MarkOccurrenceProposal> getAll(Pageable pageable);

    Optional<MarkOccurrenceProposal> findById(Long id);

    List<MarkOccurrenceProposal> findByUser(User user);

    List<MarkOccurrenceProposal> findByStatus(ProposalStatus status);

}
