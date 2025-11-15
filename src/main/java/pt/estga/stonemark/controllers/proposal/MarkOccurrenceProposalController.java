package pt.estga.stonemark.controllers.proposal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.stonemark.dtos.proposal.ProposalStateDto;
import pt.estga.stonemark.services.proposal.MarkOccurrenceProposalFlowService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/contributor/proposals/mark-occurrence")
@RequiredArgsConstructor
public class MarkOccurrenceProposalController {

    private final MarkOccurrenceProposalFlowService proposalFlowService;

    @PostMapping("/initiate")
    public ResponseEntity<ProposalStateDto> initiateProposal(@RequestParam("photo") MultipartFile photo) throws IOException {
        ProposalStateDto state = proposalFlowService.initiateProposal(photo);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{proposalId}/monument")
    public ResponseEntity<ProposalStateDto> updateMonument(
            @PathVariable Long proposalId,
            @RequestParam(value = "monumentName", required = false) String monumentName,
            @RequestParam(value = "latitude", required = false, defaultValue = "0") double latitude,
            @RequestParam(value = "longitude", required = false, defaultValue = "0") double longitude) {
        ProposalStateDto state = proposalFlowService.updateMonument(proposalId, monumentName, latitude, longitude);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{proposalId}/finalize")
    public ResponseEntity<ProposalStateDto> finalizeProposal(@PathVariable Long proposalId) {
        ProposalStateDto state = proposalFlowService.finalizeProposal(proposalId);
        return ResponseEntity.ok(state);
    }
}
