package pt.estga.decision.dtos;

import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.enums.SubmissionSource;

import java.time.Instant;
import java.util.List;

public record ProposalAdminDetailDto(
    // Proposal Core Info
    Long id,
    ProposalStatus status,
    Integer priority,
    Integer credibilityScore,
    SubmissionSource submissionSource,
    String userNotes,
    String monumentName, // Proposed monument name
    Double latitude,
    Double longitude,
    Long photoId,

    // Submission Info
    Long submittedById,
    String submittedByUsername,
    Instant submittedAt,

    // Linked Entities Info
    Long existingMonumentId,
    String existingMonumentName,
    Long existingMarkId,

    // Moderation Info
    ActiveDecisionViewDto activeDecision,
    List<DecisionHistoryItem> decisionHistory
) {
}
