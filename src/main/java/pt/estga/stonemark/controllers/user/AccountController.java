package pt.estga.stonemark.controllers.user;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.stonemark.dtos.MessageResponseDto;
import pt.estga.stonemark.dtos.account.PasswordChangeRequestDto;
import pt.estga.stonemark.dtos.account.EmailChangeRequestDto;
import pt.estga.stonemark.dtos.account.PasswordSetRequestDto;
import pt.estga.stonemark.dtos.user.UserDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.exceptions.EmailAlreadyTakenException;
import pt.estga.stonemark.mappers.UserMapper;
import pt.estga.stonemark.services.PasswordService;
import pt.estga.stonemark.services.UserService;
import pt.estga.stonemark.services.security.auth.AuthenticationService;
import pt.estga.stonemark.services.security.verification.VerificationInitiationService;

@RestController
@RequestMapping("/api/v1/user/account")
@RequiredArgsConstructor
@Tag(name = "User - Account", description = "Self-service operations for logged-in users.")
public class AccountController {

    private final UserMapper mapper;
    private final UserService userService;
    private final AuthenticationService authService;
    private final VerificationInitiationService verificationInitiationService;
    private final PasswordService passwordService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getPersonalInfo(@AuthenticationPrincipal User connectedUser) {
        if (connectedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(mapper.toDto(connectedUser));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody PasswordChangeRequestDto request,
            @AuthenticationPrincipal User connectedUser) {
        passwordService.changePassword(connectedUser, request);
        return ResponseEntity.ok(new MessageResponseDto("Your password has been changed successfully."));
    }

    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(
            @Valid @RequestBody PasswordSetRequestDto request,
            @AuthenticationPrincipal User connectedUser) {
        passwordService.setPassword(connectedUser, request);
        return ResponseEntity.ok(new MessageResponseDto("Your password has been set successfully."));
    }

    @DeleteMapping("/google")
    public ResponseEntity<?> disconnectGoogle(@AuthenticationPrincipal User user) {
        authService.disconnectGoogle(user);
        return ResponseEntity.ok(new MessageResponseDto("Your account has been successfully disconnected from Google."));
    }

    @PostMapping("/request-email-change")
    public ResponseEntity<?> requestEmailChange(
            @Valid @RequestBody EmailChangeRequestDto request,
            @AuthenticationPrincipal User user) {
        try {
            verificationInitiationService.requestEmailChange(user, request.newEmail());
            return ResponseEntity.ok(new MessageResponseDto("A confirmation email has been sent to your current email address."));
        } catch (EmailAlreadyTakenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage()));
        }
    }
}
