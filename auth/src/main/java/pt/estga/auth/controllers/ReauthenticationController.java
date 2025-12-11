package pt.estga.auth.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.estga.auth.dtos.ReauthenticationRequest;
import pt.estga.auth.services.ReauthenticationService;

@RestController
@RequestMapping("/auth/reauth")
public class ReauthenticationController {

    private final ReauthenticationService reauthenticationService;

    public ReauthenticationController(ReauthenticationService reauthenticationService) {
        this.reauthenticationService = reauthenticationService;
    }

    @PostMapping
    // Todo: implement reauthentication
    public ResponseEntity<Void> reauthenticate(@RequestBody ReauthenticationRequest reauthenticationRequest) {
        // Here you would validate the user's credentials (password or OTP)
        // For example, by calling a method in the AuthenticationService
        // If the credentials are valid:
        reauthenticationService.markSessionAsReauthenticated();
        return ResponseEntity.ok().build();
        // If the credentials are not valid, you would return a 401 Unauthorized
    }
}
