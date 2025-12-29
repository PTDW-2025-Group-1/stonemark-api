package pt.estga.proposal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarkOccurrenceProposalRepository extends JpaRepository<MarkOccurrenceProposal, Long> {

    List<MarkOccurrenceProposal> findBySubmittedById(Long userId);

    List<MarkOccurrenceProposal> findByPriorityGreaterThanEqual(Integer priority);

    Optional<MarkOccurrenceProposal> findFirstByIsSubmitted(boolean isSubmitted);

    @Query("SELECT COUNT(p) FROM MarkOccurrenceProposal p WHERE p.submittedById = :userId AND p.status = 'APPROVED'")
    long countApprovedProposalsByUserId(@Param("userId") Long userId);

}
