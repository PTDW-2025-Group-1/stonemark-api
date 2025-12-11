package pt.estga.auth.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.auth.dtos.PasswordResetRequestDto;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.verification.dtos.ResetPasswordRequestDto;
import pt.estga.verification.services.VerificationInitiationService;
import pt.estga.verification.services.VerificationProcessingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/password-reset")
@Tag(name = "Password Reset", description = "Endpoints for requesting and performing password resets.")
public class PasswordResetController {

    private final VerificationInitiationService verificationInitiationService;
    private final VerificationProcessingService verificationProcessingService;

    @Operation(summary = "Request password reset",
               description = "Initiates the password reset process by sending a verification code to the user's contact (email or phone number).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset initiated successfully. A verification code has been sent.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid contact value or user not found.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class)))
    })
    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(
            @RequestBody(description = "Request body containing the contact value (email or phone number) for password reset.",
                         required = true,
                         content = @Content(schema = @Schema(implementation = PasswordResetRequestDto.class)))
            @org.springframework.web.bind.annotation.RequestBody PasswordResetRequestDto request) {
        verificationInitiationService.initiatePasswordReset(request.contactValue());
        return ResponseEntity.ok(new MessageResponseDto("Password reset initiated. Check your contact for the verification code."));
    }

    @Operation(summary = "Reset password",
               description = "Resets the user's password using a valid verification token and a new password. The token is typically received via email or SMS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid token, expired token, or new password does not meet requirements.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Token is invalid or revoked.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class)))
    })
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody(description = "Request body containing the verification token and the new password.",
                         required = true,
                         content = @Content(schema = @Schema(implementation = ResetPasswordRequestDto.class)))
            @org.springframework.web.bind.annotation.RequestBody ResetPasswordRequestDto request) {
        verificationProcessingService.processPasswordReset(request.token(), request.newPassword());
        return ResponseEntity.ok(new MessageResponseDto("Password reset successfully."));
    }
}
