package pt.estga.auth.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @Operation(summary = "Register a new user",
               description = "Registers a new user with the provided details. An email verification might be required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully, returns authentication tokens.",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))),
            @ApiResponse(responseCode = "202", description = "User registered, but email verification is required.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input or user already exists.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid
            @RequestBody(description = "User registration details including username, email, and password.",
                         required = true,
                         content = @Content(schema = @Schema(implementation = RegisterRequestDto.class)))
            @org.springframework.web.bind.annotation.RequestBody RegisterRequestDto request) {
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

    @Operation(summary = "Authenticate user",
               description = "Authenticates a user with username and password, optionally with a TFA code. Returns JWT tokens upon successful authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, returns JWT tokens.",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials or Two-Factor Authentication required.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class)))
    })
    @PostMapping("/authenticate")
    public ResponseEntity<?> login(
            @RequestBody(description = "User authentication credentials including username, password, and optional TFA code.",
                         required = true,
                         content = @Content(schema = @Schema(implementation = AuthenticationRequestDto.class)))
            @org.springframework.web.bind.annotation.RequestBody AuthenticationRequestDto request) {
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

    @Operation(summary = "Refresh JWT token",
               description = "Refreshes the access token using a valid refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully, returns new JWT tokens.",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired refresh token.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponseDto> refreshToken(
            @RequestBody(description = "Refresh token string.",
                         required = true,
                         content = @Content(schema = @Schema(implementation = RefreshTokenRequestDto.class)))
            @org.springframework.web.bind.annotation.RequestBody RefreshTokenRequestDto request) {
        return authService.refreshToken(request.refreshToken())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(summary = "Authenticate with Google",
               description = "Authenticates a user using a Google OAuth2 token. If the user does not exist, a new account will be created.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, returns JWT tokens.",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid Google token.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class)))
    })
    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponseDto> google(
            @RequestBody(description = "Google OAuth2 ID token.",
                         required = true,
                         content = @Content(schema = @Schema(implementation = GoogleAuthenticationRequestDto.class)))
            @org.springframework.web.bind.annotation.RequestBody GoogleAuthenticationRequestDto request) {
        return socialAuthenticationService.authenticateWithGoogle(request.token())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(summary = "Authenticate with Telegram",
               description = "Authenticates a user using Telegram login data. If the user does not exist, a new account will be created.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, returns JWT tokens.",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid Telegram data.",
                    content = @Content(schema = @Schema(implementation = MessageResponseDto.class)))
    })
    @PostMapping("/telegram")
    public ResponseEntity<AuthenticationResponseDto> telegram(
            @RequestBody(description = "Telegram login data.",
                         required = true,
                         content = @Content(schema = @Schema(implementation = TelegramAuthenticationRequestDto.class)))
            @org.springframework.web.bind.annotation.RequestBody TelegramAuthenticationRequestDto request) {
        return socialAuthenticationService.authenticateWithTelegram(request.telegramData())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
