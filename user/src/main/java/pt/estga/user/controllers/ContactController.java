package pt.estga.user.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.estga.user.services.UserContactService;

@RestController
@RequestMapping("/api/v1/account/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts", description = "Endpoints for managing user contacts.")
@PreAuthorize("isAuthenticated()")
public class ContactController {

    private final UserContactService userContactService;

    @Operation(summary = "Check if a contact exists", description = "Checks if a contact exists by its value.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns true if the contact exists, false otherwise")
    })
    @GetMapping("/exists/by-value")
    public ResponseEntity<Boolean> existsByValue(
            @Parameter(description = "The value of the contact to check", required = true)
            @RequestParam String value) {
        return ResponseEntity.ok(userContactService.existsByValue(value));
    }
}
