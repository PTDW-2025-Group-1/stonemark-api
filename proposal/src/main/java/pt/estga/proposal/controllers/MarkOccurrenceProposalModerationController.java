package pt.estga.proposal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import pt.estga.proposal.dtos.DecisionHistoryItem;
import pt.estga.proposal.dtos.ManualDecisionRequest;
import pt.estga.proposal.dtos.ProposalModeratorViewDto;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.services.AutomaticDecisionService;
import pt.estga.proposal.services.DecisionActivationService;
import pt.estga.proposal.services.ManualDecisionService;
import pt.estga.proposal.services.ModeratorProposalQueryService;
import pt.estga.shared.exceptions.InvalidCredentialsException;
import pt.estga.shared.interfaces.AuthenticatedPrincipal;

import java.util.List;

@RestController
@RequestMapping("api/v1/proposals/mark-occurrences/moderation")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('REVIEWER')")
@Tag(name = "Proposal Moderation", description = "Endpoints for proposal moderation actions.")
public class MarkOccurrenceProposalModerationController {

    private final ModeratorProposalQueryService queryService;
    private final ManualDecisionService manualDecisionService;
    private final AutomaticDecisionService automaticDecisionService;
    private final DecisionActivationService decisionActivationService;

    // ==== Read Operations ====

    @Operation(summary = "List proposals for moderation",
               description = "Retrieves a paginated list of proposals, optionally filtered by status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposals retrieved successfully.")
    })
    @GetMapping
    public ResponseEntity<Page<ProposalModeratorViewDto>> getAllProposals(
            @Parameter(description = "Filter by proposal status")
            @RequestParam(required = false) List<ProposalStatus> status,
            @ParameterObject @PageableDefault(sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(queryService.getAllProposals(status, pageable));
    }

    @Operation(summary = "Get proposal details for moderation",
               description = "Retrieves detailed information about a proposal for moderation purposes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposal details retrieved successfully.",
                    content = @Content(schema = @Schema(implementation = ProposalModeratorViewDto.class))),
            @ApiResponse(responseCode = "404", description = "Proposal not found.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProposalModeratorViewDto> getProposal(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getProposal(id));
    }

    @Operation(summary = "Get decision history",
               description = "Retrieves the history of decisions made on a specific proposal.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Decision history retrieved successfully.",
                    content = @Content(schema = @Schema(implementation = DecisionHistoryItem.class))),
            @ApiResponse(responseCode = "404", description = "Proposal not found.")
    })
    @GetMapping("/{id}/history")
    public ResponseEntity<List<DecisionHistoryItem>> getDecisionHistory(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getDecisionHistory(id));
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
        manualDecisionService.createManualDecision(id, request.outcome(), request.notes(), principal.getId());
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
        automaticDecisionService.rerunAutomaticDecision(id);
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
        decisionActivationService.activateDecision(id, attemptId);
        return ResponseEntity.ok().build();
    }
}
