package pt.estga.proposal.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.estga.proposal.dtos.DecisionHistoryItem;
import pt.estga.proposal.dtos.ManualDecisionRequest;
import pt.estga.proposal.dtos.ProposalModeratorViewDto;
import pt.estga.proposal.services.AutomaticDecisionService;
import pt.estga.proposal.services.DecisionActivationService;
import pt.estga.proposal.services.ManualDecisionService;
import pt.estga.proposal.services.ModeratorProposalQueryService;
import pt.estga.shared.utils.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("api/v1/proposals/mark-occurrences")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('REVIEWER')")
public class MarkOccurrenceProposalModerationController {

    private final ModeratorProposalQueryService queryService;
    private final ManualDecisionService manualDecisionService;
    private final AutomaticDecisionService automaticDecisionService;
    private final DecisionActivationService decisionActivationService;

    // ==== Read Operations ====

    @GetMapping("/{id}")
    public ResponseEntity<ProposalModeratorViewDto> getProposal(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getProposal(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<DecisionHistoryItem>> getDecisionHistory(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getDecisionHistory(id));
    }

    // ==== Command Operations ====

    @PostMapping("/{id}/decisions/manual")
    public ResponseEntity<Void> createManualDecision(
            @PathVariable Long id,
            @RequestBody ManualDecisionRequest request
    ) {
        Long moderatorId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        manualDecisionService.createManualDecision(id, request.outcome(), request.notes(), moderatorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/decisions/automatic/rerun")
    public ResponseEntity<Void> rerunAutomaticDecision(@PathVariable Long id) {
        automaticDecisionService.rerunAutomaticDecision(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/decisions/{attemptId}/activate")
    public ResponseEntity<Void> activateDecision(
            @PathVariable Long id,
            @PathVariable Long attemptId
    ) {
        decisionActivationService.activateDecision(id, attemptId);
        return ResponseEntity.ok().build();
    }
}
