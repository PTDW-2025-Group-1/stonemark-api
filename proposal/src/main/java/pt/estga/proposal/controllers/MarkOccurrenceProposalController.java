package pt.estga.proposal.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.proposal.dtos.MarkOccurrenceProposalDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.mappers.MarkOccurrenceProposalMapper;
import pt.estga.proposal.services.MarkOccurrenceProposalService;
import pt.estga.user.entities.User;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/proposals/mark-occurrences")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrence Proposals", description = "Endpoints for querying and retrieving mark occurrence proposals.")
public class MarkOccurrenceProposalController {

    private final MarkOccurrenceProposalService proposalService;
    private final MarkOccurrenceProposalMapper markOccurrenceProposalMapper;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MarkOccurrenceProposalDto>> findByUser(@PathVariable Long userId) {
        User user = User.builder().id(userId).build();
        List<MarkOccurrenceProposal> proposals = proposalService.findByUser(user);
        return ResponseEntity.ok(proposals.stream()
                .map(markOccurrenceProposalMapper::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{proposalId}")
    public ResponseEntity<MarkOccurrenceProposalDto> findById(@PathVariable Long proposalId) {
        return proposalService.findById(proposalId)
                .map(markOccurrenceProposalMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
