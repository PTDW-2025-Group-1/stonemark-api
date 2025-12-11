package pt.estga.auth.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.estga.auth.dtos.ReauthenticationRequest;
import pt.estga.auth.services.ReauthenticationService;
import pt.estga.shared.exceptions.InvalidOtpException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/reauth")
@Tag(name = "Reauthentication", description = "Operations related to reauthentication.")
public class ReauthenticationController {

    private final ReauthenticationService reauthenticationService;

    @PostMapping
    public ResponseEntity<Void> reauthenticate(@RequestBody ReauthenticationRequest reauthenticationRequest) {
        try {
            reauthenticationService.reauthenticate(reauthenticationRequest);
            return ResponseEntity.ok().build();
        } catch (BadCredentialsException | InvalidOtpException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
