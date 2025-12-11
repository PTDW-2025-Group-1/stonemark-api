package pt.estga.auth.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ReauthenticationService.class);

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final TotpService totpService;

    public void reauthenticate(ReauthenticationRequest request) {
        log.info("Attempting reauthentication for user.");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Current authenticated username: {}", username);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Reauthentication failed: User not found for username {}", username);
                    return new BadCredentialsException("User not found.");
                });
        log.debug("User found: {}", user.getUsername());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Reauthentication failed: Invalid password for user {}", username);
            throw new BadCredentialsException("Invalid password.");
        }
        log.debug("Password successfully verified for user {}", username);

        if (user.getTfaMethod() != TfaMethod.NONE) {
            log.debug("2FA is enabled for user {}. Method: {}", username, user.getTfaMethod());
            if (request.getOtp() == null || request.getOtp().isEmpty()) {
                log.warn("Reauthentication failed: OTP is required but not provided for user {}", username);
                throw new InvalidOtpException("OTP is required for reauthentication.");
            }
            if (!totpService.isCodeValid(user.getTfaSecret(), request.getOtp())) {
                log.warn("Reauthentication failed: Invalid OTP for user {}", username);
                throw new InvalidOtpException("Invalid OTP.");
            }
            log.debug("OTP successfully verified for user {}", username);
        }

        markSessionAsReauthenticated();
        log.info("User {} successfully reauthenticated.", username);
    }

    public void markSessionAsReauthenticated() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute("reauthenticatedAt", Instant.now().getEpochSecond());
        log.debug("Session marked as reauthenticated at {}", Instant.now().getEpochSecond());
    }
}
