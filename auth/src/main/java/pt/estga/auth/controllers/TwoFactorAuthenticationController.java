package pt.estga.auth.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.auth.dtos.TfaSetupResponseDto;
import pt.estga.auth.dtos.TfaVerificationRequestDto;
import pt.estga.auth.services.tfa.TwoFactorAuthenticationService;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.user.entities.User;
import pt.estga.user.service.UserService;

@RestController
@RequestMapping("/api/v1/tfa")
@RequiredArgsConstructor
@Tag(name = "Two-Factor Authentication", description = "Operations related to Two-Factor Authentication (2FA) for user accounts.")
@PreAuthorize("isAuthenticated()")
public class TwoFactorAuthenticationController {

    private final TwoFactorAuthenticationService twoFactorAuthenticationService;
    private final UserService userService;

    @PostMapping("/setup")
    public ResponseEntity<TfaSetupResponseDto> setupTfa(@AuthenticationPrincipal User user) {
        String newSecret = twoFactorAuthenticationService.generateNewSecret();
        user.setTfaSecret(newSecret);
        userService.update(user); // Save the secret to the user
        String qrCodeImageUrl = twoFactorAuthenticationService.generateQrCode(user);
        return ResponseEntity.ok(new TfaSetupResponseDto(newSecret, qrCodeImageUrl));
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enableTfa(@AuthenticationPrincipal User user, @Valid @RequestBody TfaVerificationRequestDto request) {
        if (user.getTfaSecret() == null || !twoFactorAuthenticationService.isCodeValid(user.getTfaSecret(), request.code())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Invalid 2FA code."));
        }
        twoFactorAuthenticationService.enableTfa(user);
        return ResponseEntity.ok(new MessageResponseDto("Two-Factor Authentication enabled successfully."));
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disableTfa(@AuthenticationPrincipal User user, @Valid @RequestBody TfaVerificationRequestDto request) {
        if (!user.isTfaEnabled()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Two-Factor Authentication is not enabled."));
        }
        if (!twoFactorAuthenticationService.isCodeValid(user.getTfaSecret(), request.code())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Invalid 2FA code."));
        }
        twoFactorAuthenticationService.disableTfa(user);
        return ResponseEntity.ok(new MessageResponseDto("Two-Factor Authentication disabled successfully."));
    }
}
