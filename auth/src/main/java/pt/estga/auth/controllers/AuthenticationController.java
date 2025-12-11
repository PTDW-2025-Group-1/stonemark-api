package pt.estga.auth.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.auth.dtos.*;
import pt.estga.auth.mappers.AuthMapper;
import pt.estga.auth.services.AuthenticationService;
import pt.estga.auth.services.SocialAuthenticationService;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.shared.exceptions.EmailVerificationRequiredException;
import pt.estga.user.entities.User;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication.")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final AuthMapper authMapper;
    private final SocialAuthenticationService socialAuthenticationService;

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
        Optional<AuthenticationResponseDto> response = authService.authenticate(request.username(), request.password(), request.tfaCode());

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

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponseDto> refreshToken(@RequestBody RefreshTokenRequestDto request) {
        return authService.refreshToken(request.refreshToken())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponseDto> google(@RequestBody GoogleAuthenticationRequestDto request) {
        return socialAuthenticationService.authenticateWithGoogle(request.token())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/telegram")
    public ResponseEntity<AuthenticationResponseDto> telegram(@RequestBody TelegramAuthenticationRequestDto request) {
        return socialAuthenticationService.authenticateWithTelegram(request.telegramData())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
