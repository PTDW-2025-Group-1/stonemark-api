package pt.estga.proposal.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarkOccurrenceProposalRepository extends JpaRepository<MarkOccurrenceProposal, Long> {

    @Query("SELECT p FROM MarkOccurrenceProposal p " +
            "LEFT JOIN FETCH p.originalMediaFile " +
            "LEFT JOIN FETCH p.existingMonument " +
            "LEFT JOIN FETCH p.existingMark " +
            "WHERE p.submittedById = :userId")
    Page<MarkOccurrenceProposal> findBySubmittedById(@Param("userId") Long userId, Pageable pageable);

    List<MarkOccurrenceProposal> findByPriorityGreaterThanEqual(Integer priority);

    Optional<MarkOccurrenceProposal> findFirstBySubmitted(boolean submitted);

    @Query("SELECT COUNT(p) FROM MarkOccurrenceProposal p WHERE p.submittedById = :userId AND p.status = 'APPROVED'")
    long countApprovedProposalsByUserId(@Param("userId") Long userId);

}
