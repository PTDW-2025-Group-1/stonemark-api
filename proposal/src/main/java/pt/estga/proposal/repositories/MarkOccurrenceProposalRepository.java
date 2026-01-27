package pt.estga.proposal.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.estga.proposal.dtos.ProposalAdminListDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.projections.MarkOccurrenceProposalStatsProjection;
import pt.estga.user.entities.User;

import java.util.Collection;

@Repository
public interface MarkOccurrenceProposalRepository extends JpaRepository<MarkOccurrenceProposal, Long> {

    @Override
    @EntityGraph(attributePaths = {"existingMonument"})
    Page<MarkOccurrenceProposal> findAll(Pageable pageable);

    Page<MarkOccurrenceProposal> findBySubmittedBy(User user, Pageable pageable);

    @Query("""
    SELECT
        SUM(CASE WHEN p.status IN ('AUTO_ACCEPTED', 'MANUALLY_ACCEPTED') THEN 1 ELSE 0 END) as accepted,
        SUM(CASE WHEN p.status IN ('SUBMITTED', 'UNDER_REVIEW') THEN 1 ELSE 0 END) as underReview,
        SUM(CASE WHEN p.status IN ('AUTO_REJECTED', 'MANUALLY_REJECTED') THEN 1 ELSE 0 END) as rejected
    FROM MarkOccurrenceProposal p
    WHERE p.submittedBy.id = :userId
    """)
    MarkOccurrenceProposalStatsProjection getStatsByUserId(@Param("userId") Long userId);

    long countBySubmittedByIdAndStatusIn(Long submittedById, Collection<ProposalStatus> statuses);

    @Query("SELECT new pt.estga.proposal.dtos.ProposalAdminListDto(" +
            "p.id, p.status, p.priority, p.submissionSource, p.submittedAt, " +
            "COALESCE(m.name, p.monumentName)) " +
            "FROM MarkOccurrenceProposal p " +
            "LEFT JOIN p.existingMonument m " +
            "WHERE (:statuses IS NULL OR p.status IN :statuses)")
    Page<ProposalAdminListDto> findModeratorListDto(@Param("statuses") Collection<ProposalStatus> statuses, Pageable pageable);

}
