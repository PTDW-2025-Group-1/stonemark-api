package pt.estga.proposal.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.proposal.dtos.MarkOccurrenceProposalStatsDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.ProposalStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarkOccurrenceProposalRepository extends JpaRepository<MarkOccurrenceProposal, Long> {

    @Query("SELECT p FROM MarkOccurrenceProposal p " +
            "LEFT JOIN FETCH p.existingMonument " +
            "LEFT JOIN FETCH p.existingMark " +
            "WHERE p.submittedById = :userId")
    Page<MarkOccurrenceProposal> findBySubmittedById(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM MarkOccurrenceProposal p " +
            "LEFT JOIN FETCH p.existingMonument " +
            "LEFT JOIN FETCH p.existingMark " +
            "WHERE p.id = :id")
    Optional<MarkOccurrenceProposal> findByIdDetailed(@Param("id") Long id);

    List<MarkOccurrenceProposal> findByPriorityGreaterThanEqual(Integer priority);
    @Query("SELECT p FROM MarkOccurrenceProposal p " +
            "LEFT JOIN FETCH p.existingMonument " +
            "LEFT JOIN FETCH p.existingMark " +
            "LEFT JOIN FETCH p.originalMediaFile " +
            "WHERE p.id = :id")
    Optional<MarkOccurrenceProposal> findByIdWithDetails(@Param("id") Long id);

    Optional<MarkOccurrenceProposal> findFirstBySubmitted(boolean submitted);

    @Query("""
    SELECT new pt.estga.proposal.dtos.MarkOccurrenceProposalStatsDto(
        SUM(CASE WHEN p.status IN ('AUTO_ACCEPTED', 'MANUALLY_ACCEPTED') THEN 1 ELSE 0 END),
        SUM(CASE WHEN p.status IN ('SUBMITTED', 'UNDER_REVIEW') THEN 1 ELSE 0 END),
        SUM(CASE WHEN p.status IN ('AUTO_REJECTED', 'MANUALLY_REJECTED') THEN 1 ELSE 0 END)
    )
    FROM MarkOccurrenceProposal p
    WHERE p.submittedById = :userId
    """)
    MarkOccurrenceProposalStatsDto getStatsByUserId(@Param("userId") Long userId);

    long countBySubmittedByIdAndStatusIn(Long submittedById, Collection<ProposalStatus> statuses);

    Page<MarkOccurrenceProposal> findByStatusIn(Collection<ProposalStatus> statuses, Pageable pageable);

}
