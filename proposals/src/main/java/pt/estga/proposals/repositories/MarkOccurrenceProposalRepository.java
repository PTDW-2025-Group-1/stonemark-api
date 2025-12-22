package pt.estga.proposals.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.user.entities.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarkOccurrenceProposalRepository extends JpaRepository<MarkOccurrenceProposal, Long> {

    List<MarkOccurrenceProposal> findByCreatedBy(User user);

    List<MarkOccurrenceProposal> findByPriorityGreaterThanEqual(Integer priority);

    Optional<MarkOccurrenceProposal> findFirstByIsSubmitted(boolean isSubmitted);

    @Query("SELECT COUNT(p) FROM MarkOccurrenceProposal p WHERE p.createdBy = :user AND p.status = 'APPROVED'")
    long countApprovedProposalsByUser(@Param("user") User user);

}
