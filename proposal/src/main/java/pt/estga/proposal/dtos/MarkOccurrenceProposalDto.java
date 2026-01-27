package pt.estga.proposal.dtos;

import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.enums.SubmissionSource;

public record MarkOccurrenceProposalDto(
        Long id,
        Integer priority,
        String monumentName,
        Double latitude,
        Double longitude,
        boolean newMark,
        String userNotes,
        SubmissionSource submissionSource,
        boolean isSubmitted,
        ProposalStatus status,
        Long existingMonumentId,
        Long existingMarkId,
        Long photoId
) { }
