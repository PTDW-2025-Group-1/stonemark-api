package pt.estga.auth.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.estga.auth.dtos.ReauthenticationRequest;
import pt.estga.auth.services.tfa.TotpService;
import pt.estga.shared.exceptions.InvalidOtpException;
import pt.estga.user.entities.User;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.services.UserService;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ReauthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final TotpService totpService;

    public void reauthenticate(ReauthenticationRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password.");
        }

        if (user.getTfaMethod() != TfaMethod.NONE) {
            if (request.getOtp() == null || request.getOtp().isEmpty()) {
                throw new InvalidOtpException("OTP is required for reauthentication.");
            }
            if (!totpService.isCodeValid(user.getTfaSecret(), request.getOtp())) {
                throw new InvalidOtpException("Invalid OTP.");
            }
        }

        markSessionAsReauthenticated();
    }

    public void markSessionAsReauthenticated() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute("reauthenticatedAt", Instant.now().getEpochSecond());
    }
}
