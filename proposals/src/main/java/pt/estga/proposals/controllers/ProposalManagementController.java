package pt.estga.proposals.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.proposals.dtos.MarkOccurrenceProposalDto;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.mappers.MarkOccurrenceProposalMapper;
import pt.estga.proposals.services.MarkOccurrenceProposalManagementService;

@RestController
@RequestMapping("/api/v1/proposals/mark-occurrences/management")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrence Proposal Management", description = "Endpoints for managing mark occurrence proposals.")
public class ProposalManagementController {

    private final MarkOccurrenceProposalManagementService markOccurrenceProposalManagementService;
    private final MarkOccurrenceProposalMapper markOccurrenceProposalMapper;

    @PostMapping("/{proposalId}/approve")
    public ResponseEntity<MarkOccurrenceProposalDto> approve(@PathVariable Long proposalId) {
        MarkOccurrenceProposal proposal = markOccurrenceProposalManagementService.approve(proposalId);
        return ResponseEntity.ok(markOccurrenceProposalMapper.toDto(proposal));
    }

    @PostMapping("/{proposalId}/reject")
    public ResponseEntity<MarkOccurrenceProposalDto> reject(@PathVariable Long proposalId) {
        MarkOccurrenceProposal proposal = markOccurrenceProposalManagementService.reject(proposalId);
        return ResponseEntity.ok(markOccurrenceProposalMapper.toDto(proposal));
    }
}
