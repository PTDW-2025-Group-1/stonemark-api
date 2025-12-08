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
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.services.tfa.ContactBasedTwoFactorAuthenticationService;
import pt.estga.auth.services.tfa.TwoFactorAuthenticationService;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.user.entities.User;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.service.UserService;

@RestController
@RequestMapping("/api/v1/tfa")
@RequiredArgsConstructor
@Tag(name = "Two-Factor Authentication", description = "Operations related to Two-Factor Authentication (2FA) for user accounts.")
@PreAuthorize("isAuthenticated()")
public class TwoFactorAuthenticationController {

    private final TwoFactorAuthenticationService twoFactorAuthenticationService;
    private final ContactBasedTwoFactorAuthenticationService contactBasedTwoFactorAuthenticationService;
    private final UserService userService;

    @PostMapping("/setup/totp")
    public ResponseEntity<TfaSetupResponseDto> setupTotp(@AuthenticationPrincipal User user) {
        String newSecret = twoFactorAuthenticationService.generateNewSecret();
        user.setTfaSecret(newSecret);
        userService.update(user); // Save the secret to the user
        String qrCodeImageUrl = twoFactorAuthenticationService.generateQrCode(user);
        return ResponseEntity.ok(new TfaSetupResponseDto(newSecret, qrCodeImageUrl));
    }

    @PostMapping("/enable/totp")
    public ResponseEntity<?> enableTotp(@AuthenticationPrincipal User user, @Valid @RequestBody TfaVerificationRequestDto request) {
        if (user.getTfaSecret() == null || !twoFactorAuthenticationService.isCodeValid(user.getTfaSecret(), request.code())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Invalid 2FA code."));
        }
        twoFactorAuthenticationService.enableTfa(user, TfaMethod.TOTP);
        return ResponseEntity.ok(new MessageResponseDto("Two-Factor Authentication enabled successfully."));
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disableTfa(@AuthenticationPrincipal User user, @Valid @RequestBody TfaVerificationRequestDto request) {
        if (user.getTfaMethod() == TfaMethod.NONE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Two-Factor Authentication is not enabled."));
        }
        // For disabling, we need to verify the current active 2FA method
        boolean isValid = false;
        if (user.getTfaMethod() == TfaMethod.TOTP) {
            if (user.getTfaSecret() != null) {
                isValid = twoFactorAuthenticationService.isCodeValid(user.getTfaSecret(), request.code());
            }
        } else if (user.getTfaMethod() == TfaMethod.SMS) {
            isValid = contactBasedTwoFactorAuthenticationService.verifyCode(user, request.code(), VerificationTokenPurpose.SMS_2FA);
        } else if (user.getTfaMethod() == TfaMethod.EMAIL) {
            isValid = contactBasedTwoFactorAuthenticationService.verifyCode(user, request.code(), VerificationTokenPurpose.EMAIL_2FA);
        }

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Invalid 2FA code."));
        }

        twoFactorAuthenticationService.disableTfa(user); // This will set tfaMethod to NONE and clear secret
        return ResponseEntity.ok(new MessageResponseDto("Two-Factor Authentication disabled successfully."));
    }

    // New endpoints moved from AccountController
    @PostMapping("/method")
    public ResponseEntity<?> setTfaMethod(@AuthenticationPrincipal User user, @Valid @RequestBody SetTfaMethodRequestDto request) {
        if (request.tfaMethod() == TfaMethod.TOTP && user.getTfaSecret() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("TOTP secret not set. Please set up TOTP first."));
        }
        user.setTfaMethod(request.tfaMethod());
        userService.update(user);
        return ResponseEntity.ok(new MessageResponseDto("Two-Factor Authentication method updated successfully."));
    }

    @PostMapping("/contact/request-code")
    public ResponseEntity<?> requestContactTfaCode(@AuthenticationPrincipal User user) {
        if (user.getTfaMethod() == TfaMethod.SMS) {
            contactBasedTwoFactorAuthenticationService.generateAndSendSmsCode(user);
            return ResponseEntity.ok(new MessageResponseDto("SMS 2FA code sent to your primary telephone."));
        } else if (user.getTfaMethod() == TfaMethod.EMAIL) {
            contactBasedTwoFactorAuthenticationService.generateAndSendEmailCode(user);
            return ResponseEntity.ok(new MessageResponseDto("Email 2FA code sent to your primary email."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Contact-based 2FA is not enabled for this user."));
        }
    }

    @PostMapping("/contact/verify-code")
    public ResponseEntity<?> verifyContactTfaCode(@AuthenticationPrincipal User user, @Valid @RequestBody TfaContactVerificationRequestDto request) {
        boolean isValid;
        if (user.getTfaMethod() == TfaMethod.SMS) {
            isValid = contactBasedTwoFactorAuthenticationService.verifyCode(user, request.code(), VerificationTokenPurpose.SMS_2FA);
        } else if (user.getTfaMethod() == TfaMethod.EMAIL) {
            isValid = contactBasedTwoFactorAuthenticationService.verifyCode(user, request.code(), VerificationTokenPurpose.EMAIL_2FA);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Contact-based 2FA is not enabled for this user."));
        }

        if (isValid) {
            return ResponseEntity.ok(new MessageResponseDto("2FA code verified successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto("Invalid 2FA code."));
        }
    }
}
