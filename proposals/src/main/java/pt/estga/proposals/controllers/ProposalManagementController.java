package pt.estga.proposals.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.proposals.dtos.ProposalStateDto;
import pt.estga.proposals.dtos.UpdateProposalStatusRequestDto;
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

    @PutMapping("/{proposalId}/status")
    public ResponseEntity<ProposalStateDto> updateStatus(
            @PathVariable Long proposalId,
            @RequestBody UpdateProposalStatusRequestDto request) {
        
        MarkOccurrenceProposal proposal;
        String message;

        switch (request.status()) {
            case APPROVED -> {
                proposal = markOccurrenceProposalManagementService.approve(proposalId);
                message = "Proposal approved.";
            }
            case REJECTED -> {
                proposal = markOccurrenceProposalManagementService.reject(proposalId);
                message = "Proposal rejected.";
            }
            case PENDING -> {
                proposal = markOccurrenceProposalManagementService.pending(proposalId);
                message = "Proposal marked as pending.";
            }
            default -> throw new IllegalArgumentException("Invalid status transition requested.");
        }

        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), message));
    }
}
