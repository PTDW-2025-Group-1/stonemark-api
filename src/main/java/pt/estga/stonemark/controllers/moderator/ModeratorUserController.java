package pt.estga.stonemark.controllers.moderator;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.services.UserService;

@RestController
@RequestMapping("/api/v1/moderator/users")
@RequiredArgsConstructor
@Tag(name = "Moderator - User Oversight", description = "Read-only user listing for moderators.")
public class ModeratorUserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<Page<User>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }
}
