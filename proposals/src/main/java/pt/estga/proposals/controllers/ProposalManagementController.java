package pt.estga.proposals.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.proposals.dtos.ProposalStateDto;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.mappers.MarkOccurrenceProposalMapper;
import pt.estga.proposals.services.ProposalManagementService;

@RestController
@RequestMapping("/api/v1/proposals/mark-occurrences/management")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrence Proposal Management", description = "Endpoints for managing mark occurrence proposals.")
public class ProposalManagementController {

    private final ProposalManagementService proposalManagementService;
    private final MarkOccurrenceProposalMapper markOccurrenceProposalMapper;

    @PostMapping("/{proposalId}/approve")
    public ResponseEntity<ProposalStateDto> approve(@PathVariable Long proposalId) {
        MarkOccurrenceProposal proposal = proposalManagementService.approve(proposalId);
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "Proposal approved."));
    }

    @PostMapping("/{proposalId}/reject")
    public ResponseEntity<ProposalStateDto> reject(@PathVariable Long proposalId) {
        MarkOccurrenceProposal proposal = proposalManagementService.reject(proposalId);
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "Proposal rejected."));
    }
}
