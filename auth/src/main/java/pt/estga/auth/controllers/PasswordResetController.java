package pt.estga.auth.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.auth.dtos.PasswordResetRequestDto;
import pt.estga.auth.dtos.ResetPasswordRequestDto;
import pt.estga.auth.services.passwordreset.PasswordResetService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/password-reset")
@Tag(name = "Password Reset", description = "Endpoints for requesting and performing password resets.")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDto request) {
        passwordResetService.initiatePasswordReset(request.contactValue());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
