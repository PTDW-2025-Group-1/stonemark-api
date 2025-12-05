package pt.estga.user.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.shared.exceptions.EmailAlreadyTakenException;
import pt.estga.shared.exceptions.TelephoneAlreadyTakenException;
import pt.estga.user.UserMapper;
import pt.estga.user.dtos.*;
import pt.estga.user.entities.User;
import pt.estga.user.service.AccountManagementService;
import pt.estga.user.service.PasswordService;
import pt.estga.user.service.UserService;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Tag(name = "User Account", description = "Self-service operations for logged-in users.")
@PreAuthorize("isAuthenticated()")
public class AccountController {

    private final UserService userService;
    private final UserMapper mapper;
    private final AccountManagementService accountManagementService;
    private final PasswordService passwordService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfileInfo(@AuthenticationPrincipal User connectedUser) {
        return ResponseEntity.ok(mapper.toDto(connectedUser));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody ProfileUpdateRequestDto request,
            @AuthenticationPrincipal User user) {
        mapper.update(user, request);
        userService.update(user);
        return ResponseEntity.ok(new MessageResponseDto("Your profile has been updated successfully."));
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

        return ResponseEntity.ok(new MessageResponseDto("Your account has been successfully disconnected from Google."));
    }

    @PostMapping("/request-email-change")
    public ResponseEntity<?> requestEmailChange(
            @Valid @RequestBody EmailChangeRequestDto request,
            @AuthenticationPrincipal User user) {
        try {
            accountManagementService.requestEmailChange(user, request);
            return ResponseEntity.ok(new MessageResponseDto("A confirmation email has been sent to your new email address."));
        } catch (EmailAlreadyTakenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage()));
        }
    }

    @PostMapping("/request-telephone-change")
    public ResponseEntity<?> requestTelephoneChange(
            @Valid @RequestBody TelephoneChangeRequestDto request,
            @AuthenticationPrincipal User user) {
        try {
            accountManagementService.requestTelephoneChange(user, request);
            return ResponseEntity.ok(new MessageResponseDto("A confirmation SMS has been sent to your new telephone number."));
        } catch (TelephoneAlreadyTakenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage()));
        }
    }
}
