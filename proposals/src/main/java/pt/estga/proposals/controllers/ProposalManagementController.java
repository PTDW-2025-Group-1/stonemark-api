package pt.estga.proposals.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.proposals.dtos.MarkOccurrenceProposalDto;
import pt.estga.proposals.dtos.UpdateProposalStatusRequestDto;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.mappers.MarkOccurrenceProposalMapper;
import pt.estga.proposals.services.MarkOccurrenceProposalManagementService;
import pt.estga.shared.dtos.MessageResponseDto;

@RestController
@RequestMapping("/api/v1/proposals/mark-occurrences/management")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrence Proposal Management", description = "Endpoints for managing mark occurrence proposals.")
public class ProposalManagementController {

    private final MarkOccurrenceProposalManagementService markOccurrenceProposalManagementService;
    private final MarkOccurrenceProposalMapper markOccurrenceProposalMapper;

    @PutMapping("/{proposalId}/status")
    public ResponseEntity<MessageResponseDto> updateStatus(
            @PathVariable Long proposalId,
            @RequestBody UpdateProposalStatusRequestDto request) {

        switch (request.status()) {
            case APPROVED -> markOccurrenceProposalManagementService.approve(proposalId);
            case REJECTED -> markOccurrenceProposalManagementService.reject(proposalId);
            case PENDING -> markOccurrenceProposalManagementService.pending(proposalId);
        }

        return ResponseEntity.ok(new MessageResponseDto("Proposal status updated successfully"));
    }
}
