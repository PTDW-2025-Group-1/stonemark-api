package pt.estga.auth.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.auth.dtos.*;
import pt.estga.auth.mappers.AuthMapper;
import pt.estga.auth.services.AuthenticationService;
import pt.estga.auth.services.verification.VerificationProcessingService;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.shared.exceptions.EmailVerificationRequiredException;
import pt.estga.shared.exceptions.VerificationErrorMessages;
import pt.estga.user.entities.User;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication.")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final VerificationProcessingService verificationProcessingService;
    private final AuthMapper authMapper;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto request) {
        try {
            User parsedUser = authMapper.toUser(request);
            return authService.register(parsedUser)
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
    public ResponseEntity<?> login(@RequestBody AuthenticationRequestDto request) {
        Optional<AuthenticationResponseDto> response = authService.authenticate(request.email(), request.password(), request.tfaCode());

        if (response.isPresent()) {
            AuthenticationResponseDto authResponse = response.get();
            if (authResponse.tfaRequired()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponseDto("Two-Factor Authentication required. Please provide a valid TFA code."));
            }
            return ResponseEntity.ok(authResponse);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponseDto> google(@RequestBody GoogleAuthenticationRequestDto request) {
        return authService.authenticateWithGoogle(request.token())
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
    public ResponseEntity<ConfirmationResponseDto> confirmToken(@RequestParam("token") String token) {
        Optional<String> resultToken = verificationProcessingService.confirmToken(token);
        return resultToken.map(t -> ResponseEntity.ok(ConfirmationResponseDto.passwordResetRequired(t)))
                .orElseGet(() -> ResponseEntity.ok(ConfirmationResponseDto.success(VerificationErrorMessages.CONFIRMATION_SUCCESSFUL)));
    }

    @PostMapping("/confirm-code")
    public ResponseEntity<ConfirmationResponseDto> confirmCode(@RequestBody CodeConfirmationRequestDto request) {
        Optional<String> resultToken = verificationProcessingService.confirmCode(request.code());
        return resultToken.map(t -> ResponseEntity.ok(ConfirmationResponseDto.passwordResetRequired(t)))
                .orElseGet(() -> ResponseEntity.ok(ConfirmationResponseDto.success(VerificationErrorMessages.CONFIRMATION_SUCCESSFUL)));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDto request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
