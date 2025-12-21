package pt.estga.proposals.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.user.entities.User;

import java.util.List;

@Repository
public interface MarkOccurrenceProposalRepository extends JpaRepository<MarkOccurrenceProposal, Long> {

    List<MarkOccurrenceProposal> findByCreatedBy(User user);

}
