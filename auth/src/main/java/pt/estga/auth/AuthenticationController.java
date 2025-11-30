package pt.estga.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pt.estga.auth.dtos.*;
import pt.estga.auth.enums.ConfirmationStatus;
import pt.estga.auth.mappers.AuthMapper;
import pt.estga.auth.services.AuthenticationService;
import pt.estga.auth.services.verification.VerificationProcessingService;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.shared.exceptions.EmailVerificationRequiredException;
import pt.estga.user.Role;
import pt.estga.user.entities.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication.")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final VerificationProcessingService verificationProcessingService;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto request) {
        try {
            String rawPassword = request.password();
            User parsedUser = authMapper.toUser(request);
            parsedUser.setPassword(passwordEncoder.encode(rawPassword));
            if (parsedUser.getRole() == null) {
                parsedUser.setRole(Role.USER);
            }
            return authService.register(parsedUser, rawPassword)
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
        return authService.authenticate(request.email(), request.password())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
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
        ConfirmationResponseDto response = verificationProcessingService.processTokenConfirmation(token);
        if (ConfirmationStatus.ERROR.equals(response.status())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm-code")
    public ResponseEntity<ConfirmationResponseDto> confirmCode(@RequestBody CodeConfirmationRequestDto request) {
        ConfirmationResponseDto response = verificationProcessingService.processCodeConfirmation(request.code());
        if (ConfirmationStatus.ERROR.equals(response.status())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
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
