package pt.estga.proposals;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.proposals.dtos.ProposalStateDto;
import pt.estga.proposals.dtos.SelectExistingMarkRequestDto;
import pt.estga.proposals.dtos.ProposeNewMarkRequestDto;
import pt.estga.proposals.dtos.SelectExistingMonumentRequestDto;
import pt.estga.proposals.dtos.ProposeNewMonumentRequestDto;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.mappers.MarkOccurrenceProposalMapper;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/proposals/mark-occurrences")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrence Proposals", description = "Endpoints for mark occurrence proposals.")
public class MarkOccurrenceProposalController {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MarkOccurrenceProposalMapper markOccurrenceProposalMapper;

    @PostMapping("/initiate")
    public ResponseEntity<ProposalStateDto> initiateProposal(@RequestParam("photo") MultipartFile photo) throws IOException {
        MarkOccurrenceProposal proposal = proposalFlowService.initiateProposal(photo.getBytes(), photo.getOriginalFilename());
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "Proposal initiated."));
    }

    @PostMapping("/{proposalId}/select-existing-monument")
    public ResponseEntity<ProposalStateDto> handleExistingMonumentSelection(
            @PathVariable Long proposalId,
            @Valid @RequestBody SelectExistingMonumentRequestDto requestDto) {
        MarkOccurrenceProposal proposal = proposalFlowService.handleExistingMonumentSelection(
                proposalId,
                requestDto);
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "Existing monument selected."));
    }

    @PostMapping("/{proposalId}/propose-new-monument")
    public ResponseEntity<ProposalStateDto> handleNewMonumentProposal(
            @PathVariable Long proposalId,
            @Valid @RequestBody ProposeNewMonumentRequestDto requestDto) {
        MarkOccurrenceProposal proposal = proposalFlowService.handleNewMonumentProposal(
                proposalId,
                requestDto);
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "New monument proposed."));
    }

    @PostMapping("/{proposalId}/select-existing-mark")
    public ResponseEntity<ProposalStateDto> handleExistingMarkSelection(
            @PathVariable Long proposalId,
            @Valid @RequestBody SelectExistingMarkRequestDto requestDto) {
        MarkOccurrenceProposal proposal = proposalFlowService.handleExistingMarkSelection(
                proposalId,
                requestDto);
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "Existing mark selected."));
    }

    @PostMapping("/{proposalId}/propose-new-mark")
    public ResponseEntity<ProposalStateDto> handleNewMarkProposal(
            @PathVariable Long proposalId,
            @Valid @RequestBody ProposeNewMarkRequestDto requestDto) {
        MarkOccurrenceProposal proposal = proposalFlowService.handleNewMarkProposal(
                proposalId,
                requestDto);
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "New mark proposed."));
    }

    @PostMapping("/{proposalId}/submit")
    public ResponseEntity<ProposalStateDto> submitProposal(@PathVariable Long proposalId) {
        MarkOccurrenceProposal proposal = proposalFlowService.submitProposal(proposalId);
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "Proposal submitted."));
    }

    @PostMapping("/{proposalId}/approve")
    public ResponseEntity<ProposalStateDto> approveProposal(@PathVariable Long proposalId) {
        MarkOccurrenceProposal proposal = proposalFlowService.approveProposal(proposalId);
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "Proposal approved."));
    }

    @DeleteMapping("/{proposalId}/reject")
    public ResponseEntity<ProposalStateDto> rejectProposal(@PathVariable Long proposalId) {
        MarkOccurrenceProposal proposal = proposalFlowService.rejectProposal(proposalId);
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "Proposal rejected."));
    }
}
