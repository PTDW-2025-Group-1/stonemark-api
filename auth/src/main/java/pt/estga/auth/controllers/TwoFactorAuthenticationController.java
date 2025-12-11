package pt.estga.auth.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.auth.dtos.SetTfaMethodRequestDto;
import pt.estga.auth.dtos.TfaContactVerificationRequestDto;
import pt.estga.auth.dtos.TfaSetupResponseDto;
import pt.estga.auth.dtos.TfaVerificationRequestDto;
import pt.estga.auth.services.tfa.TwoFactorAuthenticationService;
import pt.estga.auth.services.tfa.TotpService;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.user.entities.User;
import pt.estga.user.enums.TfaMethod;

@RestController
@RequestMapping("/api/v1/auth/tfa")
@RequiredArgsConstructor
@Tag(name = "Two-Factor Authentication", description = "Operations related to Two-Factor Authentication (2FA) for user accounts.")
@PreAuthorize("isAuthenticated()")
public class TwoFactorAuthenticationController {

    private final TotpService totpService;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;

    @PostMapping("/setup/totp")
    public ResponseEntity<TfaSetupResponseDto> setupTotp(@AuthenticationPrincipal User user) {
        TfaSetupResponseDto response = totpService.setupTotpForUser(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/enable/totp")
    public ResponseEntity<?> enableTotp(@AuthenticationPrincipal User user, @Valid @RequestBody TfaVerificationRequestDto request) {
        if (user.getTfaSecret() == null || !totpService.isCodeValid(user.getTfaSecret(), request.code())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Invalid 2FA code."));
        }
        totpService.enableTfa(user, TfaMethod.TOTP);
        return ResponseEntity.ok(new MessageResponseDto("Two-Factor Authentication enabled successfully."));
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disableTfa(@AuthenticationPrincipal User user, @Valid @RequestBody TfaVerificationRequestDto request) {
        if (user.getTfaMethod() == TfaMethod.NONE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Two-Factor Authentication is not enabled."));
        }

        if (!totpService.verifyAndDisableTfa(user, request.code())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Invalid 2FA code."));
        }

        return ResponseEntity.ok(new MessageResponseDto("Two-Factor Authentication disabled successfully."));
    }

    @PostMapping("/method")
    public ResponseEntity<?> setTfaMethod(@AuthenticationPrincipal User user, @Valid @RequestBody SetTfaMethodRequestDto request) {
        if (request.tfaMethod() == TfaMethod.TOTP && user.getTfaSecret() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("TOTP secret not set. Please set up TOTP first."));
        }
        twoFactorAuthenticationService.setTfaMethod(user, request.tfaMethod());
        return ResponseEntity.ok(new MessageResponseDto("Two-Factor Authentication method updated successfully."));
    }

    @PostMapping("/contact/request-code")
    public ResponseEntity<?> requestContactTfaCode(@AuthenticationPrincipal User user) {
        try {
            twoFactorAuthenticationService.requestTfaContactCode(user);
            return ResponseEntity.ok(new MessageResponseDto("2FA code sent to your primary contact method."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage()));
        }
    }

    @PostMapping("/contact/verify-code")
    public ResponseEntity<?> verifyContactTfaCode(@AuthenticationPrincipal User user, @Valid @RequestBody TfaContactVerificationRequestDto request) {
        try {
            if (twoFactorAuthenticationService.verifyTfaContactCode(user, request.code())) {
                return ResponseEntity.ok(new MessageResponseDto("2FA code verified successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Invalid 2FA code."));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage()));
        }
    }
}
