package pt.estga.proposals.controllers;

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
@RequestMapping("/api/v1/proposals/mark-occurrences/flow")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrence Proposal Flow", description = "Endpoints for the step-by-step creation of mark occurrence proposals.")
public class ProposalFlowController {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MarkOccurrenceProposalMapper markOccurrenceProposalMapper;

    @PostMapping("/initiate")
    public ResponseEntity<ProposalStateDto> initiate(@RequestParam("photo") MultipartFile photo) throws IOException {
        MarkOccurrenceProposal proposal = proposalFlowService.initiate(photo.getBytes(), photo.getOriginalFilename());
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "Proposal initiated."));
    }

    @PostMapping("/{proposalId}/select-monument")
    public ResponseEntity<ProposalStateDto> selectMonument(
            @PathVariable Long proposalId,
            @Valid @RequestBody SelectExistingMonumentRequestDto requestDto) {
        MarkOccurrenceProposal proposal = proposalFlowService.selectMonument(
                proposalId,
                requestDto.existingMonumentId());
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "Existing monument selected."));
    }

    @PostMapping("/{proposalId}/propose-monument")
    public ResponseEntity<ProposalStateDto> proposeMonument(
            @PathVariable Long proposalId,
            @Valid @RequestBody ProposeNewMonumentRequestDto requestDto) {
        MarkOccurrenceProposal proposal = proposalFlowService.proposeMonument(
                proposalId,
                requestDto.name(),
                requestDto.latitude(),
                requestDto.longitude());
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "New monument proposed."));
    }

    @PostMapping("/{proposalId}/select-mark")
    public ResponseEntity<ProposalStateDto> selectMark(
            @PathVariable Long proposalId,
            @Valid @RequestBody SelectExistingMarkRequestDto requestDto) {
        MarkOccurrenceProposal proposal = proposalFlowService.selectMark(
                proposalId,
                requestDto.existingMarkId());
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "Existing mark selected."));
    }

    @PostMapping("/{proposalId}/propose-mark")
    public ResponseEntity<ProposalStateDto> proposeMark(
            @PathVariable Long proposalId,
            @Valid @RequestBody ProposeNewMarkRequestDto requestDto) {
        MarkOccurrenceProposal proposal = proposalFlowService.proposeMark(
                proposalId,
                requestDto.name(),
                requestDto.description());
        return ResponseEntity.ok(new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), proposal.getStatus(), "New mark proposed."));
    }
}
