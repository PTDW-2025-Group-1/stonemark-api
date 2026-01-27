package pt.estga.proposal.dtos;

import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.enums.SubmissionSource;

import java.time.Instant;

public record ProposalModeratorViewDto(
        Long id,
        ProposalStatus status,
        Integer priority,

        SubmissionSource submissionSource,
        Long submittedById,
        Instant submittedAt,

        Double latitude,
        Double longitude,
        String userNotes,

        ActiveDecisionViewDto activeDecision
) {
}
