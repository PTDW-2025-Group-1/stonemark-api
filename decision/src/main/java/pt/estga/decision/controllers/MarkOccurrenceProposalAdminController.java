package pt.estga.decision.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.content.dtos.MonumentRequestDto;
import pt.estga.decision.dtos.*;
import pt.estga.decision.entities.ProposalDecisionAttempt;
import pt.estga.decision.mappers.ProposalAdminMapper;
import pt.estga.decision.repositories.ProposalDecisionAttemptRepository;
import pt.estga.decision.services.ProposalAdminService;
import pt.estga.decision.services.MarkOccurrenceProposalDecisionService;
import pt.estga.decision.dtos.ProposalFilter;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.services.MonumentCreationService;
import pt.estga.shared.exceptions.InvalidCredentialsException;
import pt.estga.shared.exceptions.ResourceNotFoundException;
import pt.estga.shared.interfaces.AuthenticatedPrincipal;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

@RestController
@RequestMapping("/api/v1/admin/proposals/mark-occurrences")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('REVIEWER')")
@Tag(name = "Proposal Moderation", description = "Endpoints for proposal moderation actions.")
public class MarkOccurrenceProposalAdminController {

    private final MarkOccurrenceProposalDecisionService markOccurrenceProposalDecisionService;
    private final MonumentCreationService monumentCreationService;
    private final ProposalAdminService proposalAdminService;
    private final ProposalAdminMapper proposalAdminMapper;
    private final ProposalDecisionAttemptRepository attemptRepo;
    private final MarkOccurrenceProposalRepository proposalRepo;
    private final UserService userService;

    // ==== Read Operations ====

    @Operation(summary = "List proposals for moderation",
               description = "Retrieves a paginated list of proposals, optionally filtered by status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposals retrieved successfully.")
    })
    @GetMapping
    public ResponseEntity<Page<ProposalAdminListDto>> getAllProposals(
            @ParameterObject ProposalFilter filter,
            @ParameterObject @PageableDefault(sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var statuses = filter.statuses();
        if (statuses != null && statuses.isEmpty()) {
            statuses = null;
        }
        
        return ResponseEntity.ok(proposalRepo.findByFilters(statuses, filter.submittedById(), pageable)
                .map(proposalAdminMapper::toAdminListDto));
    }

    @Operation(summary = "Get full proposal details",
               description = "Retrieves comprehensive information about a proposal, including history, active decision, and autofill data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposal details retrieved successfully.",
                    content = @Content(schema = @Schema(implementation = ProposalAdminDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Proposal not found.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProposalAdminDetailDto> getProposalDetails(@PathVariable Long id) {
        return ResponseEntity.ok(proposalAdminService.getProposalDetails(id));
    }

    // ==== Command Operations ====

    @Operation(summary = "Create a manual decision",
               description = "Creates a manual decision for a proposal (Accept/Reject).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manual decision created successfully."),
            @ApiResponse(responseCode = "404", description = "Proposal not found."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")
    })
    @PostMapping("/{id}/decisions/manual")
    public ResponseEntity<Void> createManualDecision(
            @PathVariable Long id,
            @RequestBody ManualDecisionRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        if (principal == null) {
            throw new InvalidCredentialsException("User not authenticated");
        }
        User moderator = userService.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Moderator not found"));
        markOccurrenceProposalDecisionService.makeManualDecision(id, request.outcome(), request.notes(), moderator);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Rerun automatic decision",
               description = "Triggers the automatic decision logic again for a proposal.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Automatic decision rerun successfully."),
            @ApiResponse(responseCode = "404", description = "Proposal not found.")
    })
    @PostMapping("/{id}/decisions/automatic/rerun")
    public ResponseEntity<Void> rerunAutomaticDecision(@PathVariable Long id) {
        markOccurrenceProposalDecisionService.makeAutomaticDecision(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate a previous decision",
               description = "Reverts the proposal status to a previous decision attempt.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Decision activated successfully."),
            @ApiResponse(responseCode = "404", description = "Proposal or decision attempt not found."),
            @ApiResponse(responseCode = "400", description = "Decision attempt does not belong to the proposal.")
    })
    @PostMapping("/{id}/decisions/{attemptId}/activate")
    public ResponseEntity<Void> activateDecision(
            @PathVariable Long id,
            @PathVariable Long attemptId
    ) {
        ProposalDecisionAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision attempt not found with id: " + attemptId));
        
        if (!attempt.getProposal().getId().equals(id)) {
            throw new IllegalArgumentException("Decision attempt does not belong to this proposal");
        }
        
        markOccurrenceProposalDecisionService.activateDecision(attemptId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Create monument for proposal",
               description = "Creates a new monument based on the proposal data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monument created successfully."),
            @ApiResponse(responseCode = "404", description = "Proposal not found.")
    })
    @PostMapping("/{id}/monument")
    public ResponseEntity<Void> createMonumentForProposal(
            @PathVariable Long id,
            @RequestBody MonumentRequestDto request
    ) {
        monumentCreationService.createMonumentFromProposal(id, request);
        return ResponseEntity.ok().build();
    }
}
