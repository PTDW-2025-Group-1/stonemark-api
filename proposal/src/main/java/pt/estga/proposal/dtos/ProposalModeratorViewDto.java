package pt.estga.proposal.dtos;

import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.enums.SubmissionSource;

import java.time.Instant;

public record ProposalModeratorViewDto(
        Long id,
        ProposalStatus status,
        Integer priority,
        Integer credibilityScore,

        SubmissionSource submissionSource,
        Long submittedById,
        String submittedByUsername,
        Instant submittedAt,

        Double latitude,
        Double longitude,
        String userNotes,
        String monumentName,
        Long photoId,

        Long existingMonumentId,
        String existingMonumentName,
        Long existingMarkId,
        String existingMarkName,

        ActiveDecisionViewDto activeDecision
) { }
