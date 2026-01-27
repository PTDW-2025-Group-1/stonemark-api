package pt.estga.proposal.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.proposal.dtos.MarkOccurrenceProposalListDto;
import pt.estga.proposal.dtos.MarkOccurrenceProposalStatsDto;
import pt.estga.proposal.dtos.ProposalModeratorListDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.ProposalStatus;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface MarkOccurrenceProposalRepository extends JpaRepository<MarkOccurrenceProposal, Long> {

    @Override
    @EntityGraph(attributePaths = {"existingMonument"})
    Page<MarkOccurrenceProposal> findAll(Pageable pageable);

    @Query("SELECT p FROM MarkOccurrenceProposal p " +
            "LEFT JOIN FETCH p.originalMediaFile " +
            "WHERE p.submittedById = :userId")
    Page<MarkOccurrenceProposal> findBySubmittedById(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new pt.estga.proposal.dtos.MarkOccurrenceProposalListDto(" +
            "p.id, p.originalMediaFile.id, p.submitted, p.status, p.submittedAt) " +
            "FROM MarkOccurrenceProposal p " +
            "WHERE p.submittedById = :userId")
    Page<MarkOccurrenceProposalListDto> findListDtoBySubmittedById(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM MarkOccurrenceProposal p " +
            "LEFT JOIN FETCH p.existingMonument m " +
            "LEFT JOIN FETCH p.existingMark mk " +
            "LEFT JOIN FETCH p.originalMediaFile " +
            "WHERE p.submittedById = :userId")
    Page<MarkOccurrenceProposal> findDetailedBySubmittedById(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM MarkOccurrenceProposal p " +
            "LEFT JOIN FETCH p.existingMonument m " +
            "LEFT JOIN FETCH p.existingMark mk " +
            "LEFT JOIN FETCH p.originalMediaFile " +
            "WHERE p.id = :id")
    Optional<MarkOccurrenceProposal> findDetailedById(@Param("id") Long id);

    Optional<MarkOccurrenceProposal> findFirstBySubmittedByIdAndSubmitted(Long submittedById, boolean submitted);

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

    @EntityGraph(attributePaths = {"existingMonument"})
    Page<MarkOccurrenceProposal> findByStatusIn(Collection<ProposalStatus> statuses, Pageable pageable);

    @Query("SELECT new pt.estga.proposal.dtos.ProposalModeratorListDto(" +
            "p.id, p.status, p.priority, p.submissionSource, p.submittedAt, " +
            "COALESCE(m.name, p.monumentName)) " +
            "FROM MarkOccurrenceProposal p " +
            "LEFT JOIN p.existingMonument m " +
            "WHERE (:statuses IS NULL OR p.status IN :statuses)")
    Page<ProposalModeratorListDto> findModeratorListDto(@Param("statuses") Collection<ProposalStatus> statuses, Pageable pageable);

}
