package pt.estga.proposal.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.proposal.dtos.MarkOccurrenceProposalDto;
import pt.estga.proposal.dtos.MarkOccurrenceProposalListDto;
import pt.estga.proposal.dtos.MarkOccurrenceProposalStatsDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.mappers.MarkOccurrenceProposalMapper;
import pt.estga.proposal.services.MarkOccurrenceProposalService;
import pt.estga.user.entities.User;

@RestController
@RequestMapping("/api/v1/public/proposals/mark-occurrences")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrence Proposals", description = "Endpoints for querying and retrieving mark occurrence proposals.")
public class MarkOccurrenceProposalController {

    private final MarkOccurrenceProposalService proposalService;
    private final MarkOccurrenceProposalMapper markOccurrenceProposalMapper;

    @GetMapping("/user/{userId}")
    public Page<MarkOccurrenceProposalListDto> findByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        User user = User.builder().id(userId).build();
        Page<MarkOccurrenceProposal> proposals = proposalService.findByUser(user, PageRequest.of(page, size));
        return proposals.map(markOccurrenceProposalMapper::toListDto);
    }

    @GetMapping("/user/{userId}/detailed")
    public Page<MarkOccurrenceProposalDto> findDetailedByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        User user = User.builder().id(userId).build();
        Page<MarkOccurrenceProposal> proposals = proposalService.findByUser(user, PageRequest.of(page, size));
        return proposals.map(markOccurrenceProposalMapper::toDto);
    }

    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<MarkOccurrenceProposalStatsDto> getUserStats(
            @PathVariable Long userId
    ) {
        User user = User.builder().id(userId).build();
        return ResponseEntity.ok(proposalService.getStatsByUser(user));
    }

    @GetMapping("/{proposalId}")
    public ResponseEntity<MarkOccurrenceProposalDto> findById(@PathVariable Long proposalId) {
        return proposalService.findById(proposalId)
                .map(markOccurrenceProposalMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
