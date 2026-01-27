package pt.estga.proposal.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.proposal.dtos.MarkOccurrenceProposalStatsDto;
import pt.estga.proposal.dtos.ProposalModeratorListDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.user.entities.User;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface MarkOccurrenceProposalRepository extends JpaRepository<MarkOccurrenceProposal, Long> {

    @Override
    @EntityGraph(attributePaths = {"existingMonument"})
    Page<MarkOccurrenceProposal> findAll(Pageable pageable);

    Page<MarkOccurrenceProposal> findBySubmittedBy(User user, Pageable pageable);

    Optional<MarkOccurrenceProposal> findFirstBySubmittedByIdAndSubmitted(Long submittedById, boolean submitted);

    @Query("""
    SELECT new pt.estga.proposal.dtos.MarkOccurrenceProposalStatsDto(
        SUM(CASE WHEN p.status IN ('AUTO_ACCEPTED', 'MANUALLY_ACCEPTED') THEN 1 ELSE 0 END),
        SUM(CASE WHEN p.status IN ('SUBMITTED', 'UNDER_REVIEW') THEN 1 ELSE 0 END),
        SUM(CASE WHEN p.status IN ('AUTO_REJECTED', 'MANUALLY_REJECTED') THEN 1 ELSE 0 END)
    )
    FROM MarkOccurrenceProposal p
    WHERE p.submittedBy.id = :userId
    """)
    MarkOccurrenceProposalStatsDto getStatsByUserId(@Param("userId") Long userId);

    long countBySubmittedByIdAndStatusIn(Long submittedById, Collection<ProposalStatus> statuses);

    @Query("SELECT new pt.estga.proposal.dtos.ProposalModeratorListDto(" +
            "p.id, p.status, p.priority, p.submissionSource, p.submittedAt, " +
            "COALESCE(m.name, p.monumentName)) " +
            "FROM MarkOccurrenceProposal p " +
            "LEFT JOIN p.existingMonument m " +
            "WHERE (:statuses IS NULL OR p.status IN :statuses)")
    Page<ProposalModeratorListDto> findModeratorListDto(@Param("statuses") Collection<ProposalStatus> statuses, Pageable pageable);

}
