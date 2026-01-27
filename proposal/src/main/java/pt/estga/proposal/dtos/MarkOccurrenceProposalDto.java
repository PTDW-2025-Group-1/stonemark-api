package pt.estga.proposal.dtos;

import pt.estga.content.dtos.MarkDto;
import pt.estga.content.dtos.MonumentListDto;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.enums.SubmissionSource;

public record MarkOccurrenceProposalDto(
        Long id,
        Integer priority,
        Long photoId,
        MonumentListDto existingMonument,
        String monumentName,
        Double latitude,
        Double longitude,
        MarkDto existingMark,
        boolean newMark,
        String userNotes,
        SubmissionSource submissionSource,
        boolean isSubmitted,
        ProposalStatus status
) {
}
