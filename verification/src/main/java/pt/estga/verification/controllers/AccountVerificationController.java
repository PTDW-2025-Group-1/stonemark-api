package pt.estga.verification.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.verification.dtos.ConfirmationResponseDto;
import pt.estga.verification.dtos.VerificationRequestDto;
import pt.estga.verification.services.VerificationProcessingService;
import pt.estga.shared.exceptions.VerificationErrorMessages;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/account-verification")
@Tag(name = "Account Verification", description = "Endpoints for user account verification.")
public class AccountVerificationController {

    private final VerificationProcessingService verificationProcessingService;

    @PostMapping("/confirm")
    public ResponseEntity<ConfirmationResponseDto> confirm(@RequestBody VerificationRequestDto request) {
        Optional<String> resultToken = verificationProcessingService.confirmCode(request.code());
        return resultToken.map(t -> ResponseEntity.ok(ConfirmationResponseDto.passwordResetRequired(t)))
                .orElseGet(() -> ResponseEntity.ok(ConfirmationResponseDto.success(VerificationErrorMessages.CONFIRMATION_SUCCESSFUL)));
    }
}
