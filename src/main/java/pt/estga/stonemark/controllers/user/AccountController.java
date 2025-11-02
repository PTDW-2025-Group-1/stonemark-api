package pt.estga.stonemark.controllers.user;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pt.estga.stonemark.dtos.UserDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.mappers.UserMapper;

@RestController
@RequestMapping("/api/v1/user/account")
@RequiredArgsConstructor
@Tag(name = "User - Account", description = "Self-service operations for logged-in users.")
public class AccountController {

    private final UserMapper mapper;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getPersonalInfo(@AuthenticationPrincipal User connectedUser) {
        if (connectedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(mapper.toDto(connectedUser));
    }
}
