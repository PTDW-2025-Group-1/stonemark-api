package pt.estga.stonemark.controllers.proposals;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.stonemark.dtos.proposal.ProposalStateDto;
import pt.estga.stonemark.dtos.proposal.SelectExistingMarkRequestDto;
import pt.estga.stonemark.dtos.proposal.ProposeNewMarkRequestDto;
import pt.estga.stonemark.dtos.proposal.SelectExistingMonumentRequestDto;
import pt.estga.stonemark.dtos.proposal.ProposeNewMonumentRequestDto;
import pt.estga.stonemark.services.proposal.MarkOccurrenceProposalFlowService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/contributor/proposals/mark-occurrences")
@RequiredArgsConstructor
public class MarkOccurrenceProposalController {

    private final MarkOccurrenceProposalFlowService proposalFlowService;

    @PostMapping("/initiate")
    public ResponseEntity<ProposalStateDto> initiateProposal(@RequestParam("photo") MultipartFile photo) throws IOException {
        ProposalStateDto state = proposalFlowService.initiateProposal(photo.getBytes(), photo.getOriginalFilename());
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{proposalId}/select-existing-monument")
    public ResponseEntity<ProposalStateDto> handleExistingMonumentSelection(
            @PathVariable Long proposalId,
            @Valid @RequestBody SelectExistingMonumentRequestDto requestDto) {
        ProposalStateDto state = proposalFlowService.handleExistingMonumentSelection(
                proposalId,
                requestDto);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{proposalId}/propose-new-monument")
    public ResponseEntity<ProposalStateDto> handleNewMonumentProposal(
            @PathVariable Long proposalId,
            @Valid @RequestBody ProposeNewMonumentRequestDto requestDto) {
        ProposalStateDto state = proposalFlowService.handleNewMonumentProposal(
                proposalId,
                requestDto);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{proposalId}/select-existing-mark")
    public ResponseEntity<ProposalStateDto> handleExistingMarkSelection(
            @PathVariable Long proposalId,
            @Valid @RequestBody SelectExistingMarkRequestDto requestDto) {
        ProposalStateDto state = proposalFlowService.handleExistingMarkSelection(
                proposalId,
                requestDto);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{proposalId}/propose-new-mark")
    public ResponseEntity<ProposalStateDto> handleNewMarkProposal(
            @PathVariable Long proposalId,
            @Valid @RequestBody ProposeNewMarkRequestDto requestDto) {
        ProposalStateDto state = proposalFlowService.handleNewMarkProposal(
                proposalId,
                requestDto);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{proposalId}/submit")
    public ResponseEntity<ProposalStateDto> submitProposal(@PathVariable Long proposalId) {
        ProposalStateDto state = proposalFlowService.submitProposal(proposalId);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{proposalId}/approve")
    public ResponseEntity<ProposalStateDto> approveProposal(@PathVariable Long proposalId) {
        ProposalStateDto state = proposalFlowService.approveProposal(proposalId);
        return ResponseEntity.ok(state);
    }

    @DeleteMapping("/{proposalId}/reject")
    public ResponseEntity<ProposalStateDto> rejectProposal(@PathVariable Long proposalId) {
        ProposalStateDto state = proposalFlowService.rejectProposal(proposalId);
        return ResponseEntity.ok(state);
    }
}
