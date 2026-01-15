package pt.estga.proposal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.estga.content.dtos.GeocodingResultDto;
import pt.estga.content.dtos.MonumentRequestDto;
import pt.estga.content.entities.Monument;
import pt.estga.content.mappers.MonumentMapper;
import pt.estga.proposal.services.MonumentCreationService;

@RestController
@RequestMapping("api/v1/proposals/mark-occurrences")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('REVIEWER')")
@Tag(name = "Proposal Monument Management", description = "Endpoints for managing monuments related to mark occurrence proposals.")
public class MarkOccurrenceProposalMonumentController {

    private final MonumentCreationService monumentCreationService;
    private final MonumentMapper monumentMapper;

    @Operation(summary = "Get monument autofill data",
               description = "Retrieves autofill data for creating a monument from a proposal.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autofill data retrieved successfully.",
                    content = @Content(schema = @Schema(implementation = GeocodingResultDto.class))),
            @ApiResponse(responseCode = "404", description = "Proposal not found.")
    })
    @GetMapping("/{id}/monument-autofill")
    public ResponseEntity<GeocodingResultDto> getMonumentAutofillData(@PathVariable Long id) {
        return ResponseEntity.ok(monumentCreationService.getAutofillData(id));
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
        Monument monument = monumentMapper.toEntity(request);
        monumentCreationService.createMonumentFromProposal(id, monument);
        return ResponseEntity.ok().build();
    }
}
