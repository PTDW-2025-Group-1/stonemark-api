package pt.estga.auth.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.auth.dtos.CodeConfirmationRequestDto;
import pt.estga.auth.dtos.ConfirmationResponseDto;
import pt.estga.auth.services.verification.VerificationProcessingService;
import pt.estga.shared.exceptions.VerificationErrorMessages;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/account-verification")
@Tag(name = "Account Verification", description = "Endpoints for user account verification.")
public class AccountVerificationController {

    private final VerificationProcessingService verificationProcessingService;

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
}
