package pt.estga.proposal.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.proposal.dtos.MarkOccurrenceProposalDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.mappers.MarkOccurrenceProposalMapper;
import pt.estga.proposal.services.MarkOccurrenceProposalSubmissionService;

@RestController
@RequestMapping("/api/v1/proposals/mark-occurrences/submission")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrence Proposal Submission", description = "Endpoints for submitting mark occurrence proposals.")
public class ProposalSubmissionController {

    private final MarkOccurrenceProposalSubmissionService markOccurrenceProposalSubmissionService;
    private final MarkOccurrenceProposalMapper markOccurrenceProposalMapper;

    @PostMapping("/{proposalId}/submit")
    public ResponseEntity<MarkOccurrenceProposalDto> submit(@PathVariable Long proposalId) {
        MarkOccurrenceProposal proposal = markOccurrenceProposalSubmissionService.submit(proposalId);
        return ResponseEntity.ok(markOccurrenceProposalMapper.toDto(proposal));
    }
}
