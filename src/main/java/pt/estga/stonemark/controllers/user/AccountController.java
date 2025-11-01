package pt.estga.stonemark.controllers.user;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.estga.stonemark.dtos.auth.ChangePasswordRequestDto;
import pt.estga.stonemark.dtos.UserDto;
import pt.estga.stonemark.mappers.UserMapper;
import pt.estga.stonemark.services.UserService;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user/account")
@RequiredArgsConstructor
@Tag(name = "User - Account", description = "Self-service operations for logged-in users.")
public class AccountController {

    private final UserService service;
    private final UserMapper mapper;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getPersonalInfo(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return service.findByEmail(principal.getName())
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequestDto request,
            Principal connectedUser
    ) {
        service.processPasswordChangeRequest(request, connectedUser);
        return ResponseEntity.ok().build();
    }
}
