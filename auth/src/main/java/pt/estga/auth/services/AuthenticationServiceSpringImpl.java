package pt.estga.auth.services;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.auth.dtos.AuthenticationResponseDto;
import pt.estga.auth.services.tfa.TwoFactorAuthenticationService;
import pt.estga.auth.services.tfa.TotpService;
import pt.estga.security.services.AccessTokenService;
import pt.estga.security.services.JwtService;
import pt.estga.security.services.RefreshTokenService;
import pt.estga.shared.exceptions.EmailVerificationRequiredException;
import pt.estga.shared.exceptions.UsernameAlreadyTakenException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.Role;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.services.UserContactService;
import pt.estga.user.services.UserService;
import pt.estga.verification.enums.ActionCodeType;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = {EmailVerificationRequiredException.class})
public class AuthenticationServiceSpringImpl implements AuthenticationService {

    private final UserService userService;
    private final UserContactService userContactService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;

    @Value("${application.security.contact.verification-required:false}")
    private boolean contactVerificationRequired;

    @Override
    public Optional<AuthenticationResponseDto> register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }

        if (userService.existsByUsername(user.getUsername())) {
            throw new UsernameAlreadyTakenException("Username already in use");
        }

        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        user.setTfaMethod(TfaMethod.NONE);
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getContacts().clear();

        User createdUser = userService.create(user);

        return generateAuthenticationResponse(createdUser, false, false);
    }

    @NotNull
    private Optional<AuthenticationResponseDto> generateAuthenticationResponse(User user, boolean tfaRequired, boolean tfaCodeSent) {
        return getAuthenticationResponseDto(user, tfaRequired, tfaCodeSent, jwtService, refreshTokenService, accessTokenService);
    }

    @NotNull
    public static Optional<AuthenticationResponseDto> getAuthenticationResponseDto(User user, boolean tfaRequired, boolean tfaCodeSent, JwtService jwtService, RefreshTokenService refreshTokenService, AccessTokenService accessTokenService) {
        if (tfaRequired) {
            return Optional.of(new AuthenticationResponseDto(null, null, user.getRole().name(), user.getTfaMethod() != TfaMethod.NONE, true, tfaCodeSent));
        }

        var refreshTokenString = jwtService.generateRefreshToken(user.getId());
        var accessTokenString = jwtService.generateAccessToken(user.getId());

        var refreshToken = refreshTokenService.createToken(user.getId(), refreshTokenString);
        accessTokenService.createToken(user.getId(), accessTokenString, refreshToken);

        return Optional.of(new AuthenticationResponseDto(accessTokenString, refreshTokenString, user.getRole().name(), user.getTfaMethod() != TfaMethod.NONE, false, tfaCodeSent));
    }

    @Override
    public Optional<AuthenticationResponseDto> authenticate(String usernameOrContact, String password, String tfaCode) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usernameOrContact, password));
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", usernameOrContact, e);
            return Optional.empty();
        }

        User user = userService.findByUsername(usernameOrContact)
                .orElseGet(() -> userContactService.findByValue(usernameOrContact)
                        .map(UserContact::getUser)
                        .orElseThrow(() -> new IllegalArgumentException("User not found")));

        if (contactVerificationRequired && !user.isEnabled()) {
            throw new EmailVerificationRequiredException("Email verification required.");
        }

        boolean tfaCodeSent = false;
        if (user.getTfaMethod() != TfaMethod.NONE) {
            if (tfaCode == null || tfaCode.isBlank()) {
                // 2FA is enabled, but no code provided. Initiate sending a code based on method.
                switch (user.getTfaMethod()) {
                    case TOTP:
                        // For TOTP, the user is expected to provide the code from their app.
                        // If no code is provided, we just indicate it's required.
                        break;
                    case SMS:
                        twoFactorAuthenticationService.generateAndSendSmsCode(user);
                        tfaCodeSent = true;
                        break;
                    case EMAIL:
                        twoFactorAuthenticationService.generateAndSendEmailCode(user);
                        tfaCodeSent = true;
                        break;
                }
                return generateAuthenticationResponse(user, true, tfaCodeSent); // Indicate that 2FA is required
            }

            // A 2FA code was provided, verify it.
            boolean isCodeValid = switch (user.getTfaMethod()) {
                case TOTP -> totpService.isCodeValid(user.getTfaSecret(), tfaCode);
                case SMS, EMAIL ->
                        twoFactorAuthenticationService.verifyCode(user, tfaCode, ActionCodeType.TWO_FACTOR);
                default -> false;
            };

            if (!isCodeValid) {
                log.warn("Invalid 2FA code for user: {}", usernameOrContact);
                return Optional.empty(); // Invalid 2FA code
            }
        }

        return generateAuthenticationResponse(user, false, false);
    }


    @Override
    @Transactional
    public Optional<AuthenticationResponseDto> refreshToken(String refreshTokenString) {
        return refreshTokenService.findByToken(refreshTokenString)
                .filter(token -> !token.isRevoked())
                .filter(refreshToken -> jwtService.isTokenValid(refreshTokenString, refreshToken.getUserId()))
                .flatMap(refreshToken -> userService.findById(refreshToken.getUserId())
                        .map(user -> {
                            accessTokenService.revokeAllByRefreshToken(refreshToken);

                            String newAccessToken = jwtService.generateAccessToken(user.getId());
                            accessTokenService.createToken(user.getId(), newAccessToken, refreshToken);

                            return new AuthenticationResponseDto(newAccessToken, refreshTokenString, user.getRole().name(), user.getTfaMethod() != TfaMethod.NONE, false, false);
                        }));
    }

    @Override
    public void logoutFromAllDevices(User user) {
        refreshTokenService.revokeAllByUserId(user.getId());
        accessTokenService.revokeAllByUserId(user.getId());
    }
}
