package pt.estga.proposal.dtos;

import pt.estga.content.dtos.MarkDto;
import pt.estga.content.dtos.MonumentResponseDto;
import pt.estga.file.dtos.MediaFileDto;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.enums.SubmissionSource;

public record MarkOccurrenceProposalDto(
        Long id,
        Integer priority,
        SubmissionSource submissionSource,
        Long coverId,
        MonumentResponseDto existingMonument,
        String monumentName,
        Double latitude,
        Double longitude,
        MarkDto existingMark,
        boolean newMark,
        String userNotes,
        boolean isSubmitted,
        ProposalStatus status
) {
}
