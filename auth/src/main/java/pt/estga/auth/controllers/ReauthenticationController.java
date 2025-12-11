package pt.estga.auth.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.estga.auth.dtos.ReauthenticationRequest;
import pt.estga.auth.services.ReauthenticationService;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.shared.exceptions.InvalidOtpException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/reauthenticate")
@Tag(name = "Reauthentication", description = "Operations related to reauthentication.")
public class ReauthenticationController {

    private final ReauthenticationService reauthenticationService;

    @Operation(summary = "Reauthenticate user",
               description = "Reauthenticates the user with their password or a valid OTP, typically for sensitive operations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reauthentication successful.",
                    content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials or OTP.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<Void> reauthenticate(
            @RequestBody(description = "Reauthentication request containing either password or OTP.",
                         required = true,
                         content = @Content(schema = @Schema(implementation = ReauthenticationRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody ReauthenticationRequest reauthenticationRequest) {
        try {
            reauthenticationService.reauthenticate(reauthenticationRequest);
            return ResponseEntity.ok().build();
        } catch (BadCredentialsException | InvalidOtpException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
