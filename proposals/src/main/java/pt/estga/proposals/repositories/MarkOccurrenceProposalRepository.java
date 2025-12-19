package pt.estga.proposals.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalPriority;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.user.entities.User;

import java.util.List;

@Repository
public interface MarkOccurrenceProposalRepository extends JpaRepository<MarkOccurrenceProposal, Long> {

    List<MarkOccurrenceProposal> findByCreatedBy(User user);

    List<MarkOccurrenceProposal> findByStatus(ProposalStatus status);

    List<MarkOccurrenceProposal> findByPriority(ProposalPriority priority);

}
