package pt.estga.user.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.shared.aop.SensitiveOperation;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.user.dtos.ContactDto;
import pt.estga.user.dtos.UserContactDto;
import pt.estga.user.entities.User;
import pt.estga.user.services.AccountService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/account/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts", description = "Management of user contacts.")
@PreAuthorize("isAuthenticated()")
public class ContactController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<?> addContact(
            @Valid @RequestBody ContactDto request,
            @AuthenticationPrincipal User user) {
        accountService.addContact(user, request.value(), request.type());
        return ResponseEntity.ok(new MessageResponseDto("A new contact has been added to your account."));
    }

    @GetMapping
    public ResponseEntity<List<UserContactDto>> getContacts(@AuthenticationPrincipal User user) {
        List<UserContactDto> contactDtos = accountService.getContacts(user).stream()
                .map(UserContactDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(contactDtos);
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<?> requestContactVerification(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        accountService.requestContactVerification(user, id);
        return ResponseEntity.ok(new MessageResponseDto("A verification message has been sent."));
    }
    
    @SensitiveOperation
    @DeleteMapping("/{contactId}")
    public ResponseEntity<?> deleteContact(
            @PathVariable Long contactId,
            @AuthenticationPrincipal User user) {
        accountService.deleteContact(user, contactId);
        return ResponseEntity.ok(new MessageResponseDto("Contact has been deleted from your account."));
    }
}
