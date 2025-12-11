package pt.estga.user.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.shared.dtos.MessageResponseDto;
import pt.estga.user.dtos.LinkGoogleRequestDto;
import pt.estga.user.entities.User;
import pt.estga.user.enums.Provider;
import pt.estga.user.services.AccountService;

@RestController
@RequestMapping("/api/v1/account/socials")
@RequiredArgsConstructor
@Tag(name = "Social Accounts", description = "Management of social accounts linked to the user.")
@PreAuthorize("isAuthenticated()")
public class AccountSocialController {

    private final AccountService accountService;

    @PostMapping("/google")
    @Operation(
            summary = "Link Google Account",
            description = "Links a Google account to the currently authenticated user using an ID token."
    )
    @ApiResponse(responseCode = "200", description = "Google account linked successfully")
    public ResponseEntity<MessageResponseDto> linkGoogle(
            @RequestBody LinkGoogleRequestDto request,
            @AuthenticationPrincipal User user) {
        accountService.linkGoogleAccount(user, request.token());
        return ResponseEntity.ok(new MessageResponseDto("Your account has been successfully linked with Google."));
    }

    @DeleteMapping("/google")
    @Operation(summary = "Unlink Google Account", description = "Disconnects the Google account from the current user.")
    @ApiResponse(responseCode = "200", description = "Google account disconnected successfully")
    public ResponseEntity<MessageResponseDto> disconnectGoogle(@AuthenticationPrincipal User user) {
        accountService.unlinkSocialAccount(user, Provider.GOOGLE);
        return ResponseEntity.ok(new MessageResponseDto("Your account has been successfully disconnected from Google."));
    }
}
