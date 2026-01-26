package pt.estga.proposal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import pt.estga.proposal.services.MarkOccurrenceProposalSubmissionService;
import pt.estga.user.entities.User;

@RestController
@RequestMapping("/api/v1/public/proposals/mark-occurrences")
@RequiredArgsConstructor
@Tag(name = "Mark Occurrence Proposals", description = "Endpoints for querying, retrieving, and submitting mark occurrence proposals.")
public class MarkOccurrenceProposalController {

    private final MarkOccurrenceProposalService proposalService;
    private final MarkOccurrenceProposalSubmissionService submissionService;
    private final MarkOccurrenceProposalMapper markOccurrenceProposalMapper;

    @Operation(summary = "List proposals by user",
            description = "Retrieves a paginated list of mark occurrence proposals submitted by a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposals retrieved successfully.")
    })
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

    @Operation(summary = "List detailed proposals by user",
            description = "Retrieves a paginated list of detailed mark occurrence proposals submitted by a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detailed proposals retrieved successfully.")
    })
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

    @Operation(summary = "Get user proposal statistics",
            description = "Retrieves statistics about mark occurrence proposals for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully.",
                    content = @Content(schema = @Schema(implementation = MarkOccurrenceProposalStatsDto.class)))
    })
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<MarkOccurrenceProposalStatsDto> getUserStats(
            @PathVariable Long userId
    ) {
        User user = User.builder().id(userId).build();
        return ResponseEntity.ok(proposalService.getStatsByUser(user));
    }

    @Operation(summary = "Get proposal by ID",
            description = "Retrieves a specific mark occurrence proposal by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposal found.",
                    content = @Content(schema = @Schema(implementation = MarkOccurrenceProposalDto.class))),
            @ApiResponse(responseCode = "404", description = "Proposal not found.")
    })
    @GetMapping("/{proposalId}")
    public ResponseEntity<MarkOccurrenceProposalDto> findById(@PathVariable Long proposalId) {
        return proposalService.findById(proposalId)
                .map(markOccurrenceProposalMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Submit a proposal",
            description = "Submits a mark occurrence proposal for review. This changes the proposal status to SUBMITTED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposal submitted successfully.",
                    content = @Content(schema = @Schema(implementation = MarkOccurrenceProposalDto.class))),
            @ApiResponse(responseCode = "404", description = "Proposal not found."),
            @ApiResponse(responseCode = "400", description = "Proposal cannot be submitted (e.g., invalid state).")
    })
    @PostMapping("/{proposalId}/submit")
    public ResponseEntity<MarkOccurrenceProposalDto> submit(@PathVariable Long proposalId) {
        MarkOccurrenceProposal proposal = submissionService.submit(proposalId);
        return ResponseEntity.ok(markOccurrenceProposalMapper.toDto(proposal));
    }
}
