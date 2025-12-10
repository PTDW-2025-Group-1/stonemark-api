package pt.estga.user.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.user.dtos.ContactDto;
import pt.estga.user.dtos.ContactVerificationRequestDto;
import pt.estga.user.entities.User;
import pt.estga.user.services.AccountService;

@RestController
@RequestMapping("/api/v1/account/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts", description = "Management of user contacts.")
@PreAuthorize("isAuthenticated()")
public class ContactController {

    private final AccountService accountService;

    @PostMapping("/verify")
    public ResponseEntity<?> requestContactVerification(
            @Valid @RequestBody ContactVerificationRequestDto request,
            @AuthenticationPrincipal User user) {
        accountService.requestContactVerification(user, request.value(), request.type());
        return ResponseEntity.ok(new MessageResponseDto("A verification message has been sent."));
    }

    @PostMapping("/contacts")
    public ResponseEntity<?> addContact(
            @Valid @RequestBody ContactDto request,
            @AuthenticationPrincipal User user) {
        accountService.addContact(user, request.value(), request.type());
        return ResponseEntity.ok(new MessageResponseDto("A new contact has been added to your account."));
    }
}
