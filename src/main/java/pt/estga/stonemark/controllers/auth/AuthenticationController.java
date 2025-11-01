package pt.estga.stonemark.controllers.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.stonemark.dtos.MessageResponseDto;
import pt.estga.stonemark.dtos.auth.*;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.exceptions.EmailVerificationRequiredException;
import pt.estga.stonemark.services.auth.AuthenticationService;
import pt.estga.stonemark.services.auth.VerificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final VerificationService verificationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto request) {
        try {
            return authService.register(request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        } catch (EmailVerificationRequiredException e) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new MessageResponseDto(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDto(e.getMessage()));
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDto> login(@RequestBody AuthenticationRequestDto request) {
        return authService.authenticate(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponseDto> refreshToken(@RequestBody RefreshTokenRequestDto request) {
        return authService.refreshToken(request.refreshToken())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam("token") String token) {
        verificationService.processTokenConfirmation(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDto request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDto request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/request-email-change")
    public ResponseEntity<?> requestEmailChange(
            @Valid @RequestBody EmailChangeRequestDto request,
            @AuthenticationPrincipal User user) {
        verificationService.requestEmailChange(user, request.newEmail());
        return ResponseEntity.ok(new MessageResponseDto("A confirmation email has been sent to your current email address."));
    }
}
