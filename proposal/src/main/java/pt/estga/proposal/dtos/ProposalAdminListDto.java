package pt.estga.proposal.dtos;

import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.enums.SubmissionSource;

import java.time.Instant;

public record ProposalAdminListDto(
        Long id,
        ProposalStatus status,
        Integer priority,
        SubmissionSource submissionSource,
        Instant submittedAt
) { }
