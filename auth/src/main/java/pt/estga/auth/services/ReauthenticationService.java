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
import pt.estga.auth.services.tfa.TwoFactorAuthenticationService;
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
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;

    public void reauthenticate(ReauthenticationRequest request) {

        log.info("Attempting reauthentication for sensitive operation.");

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        log.debug("Authenticated username: {}", username);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Reauthentication failed: user not found ({})", username);
                    return new BadCredentialsException("User not found.");
                });

        log.debug("User found: {}", user.getUsername());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Reauthentication failed: invalid password for user {}", username);
            throw new BadCredentialsException("Invalid password.");
        }

        log.debug("Password successfully verified for user {}", username);

        TfaMethod tfaMethod = user.getTfaMethod();
        log.debug("2FA method for user {}: {}", username, tfaMethod);

        switch (tfaMethod) {

            case NONE:
                log.debug("No 2FA enabled for user {}", username);
                break;

            case TOTP:
                log.debug("TOTP (App) 2FA enabled for user {}", username);

                if (request.getOtp() == null || request.getOtp().isBlank()) {
                    log.warn("Reauthentication requires TOTP code for user {}", username);
                    throw new InvalidOtpException("OTP_REQUIRED");
                }

                if (!totpService.isCodeValid(user.getTfaSecret(), request.getOtp())) {
                    log.warn("Invalid TOTP code provided for user {}", username);
                    throw new InvalidOtpException("INVALID_OTP");
                }

                log.debug("TOTP code successfully verified for user {}", username);
                break;

            case EMAIL:
            case SMS:
                log.debug("{} 2FA enabled for user {}", tfaMethod, username);

                if (request.getOtp() == null || request.getOtp().isBlank()) {
                    log.info("OTP not provided, sending {} OTP to user {}", tfaMethod, username);
                    twoFactorAuthenticationService.requestTfaContactCode(user);
                    throw new InvalidOtpException("OTP_REQUIRED");
                }

                if (!twoFactorAuthenticationService.verifyTfaContactCode(user, request.getOtp())) {
                    log.warn("Invalid {} OTP provided for user {}", tfaMethod, username);
                    throw new InvalidOtpException("INVALID_OTP");
                }

                log.debug("{} OTP successfully verified for user {}", tfaMethod, username);
                break;
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
