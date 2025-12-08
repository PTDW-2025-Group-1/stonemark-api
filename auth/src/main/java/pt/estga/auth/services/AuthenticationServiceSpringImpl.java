package pt.estga.auth.services;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.auth.entities.token.RefreshToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.services.tfa.ContactBasedTwoFactorAuthenticationService;
import pt.estga.auth.services.tfa.TwoFactorAuthenticationService;
import pt.estga.auth.services.token.AccessTokenService;
import pt.estga.auth.services.token.RefreshTokenService;
import pt.estga.auth.services.verification.VerificationInitiationService;
import pt.estga.auth.services.verification.VerificationProcessingService;
import pt.estga.auth.services.verification.commands.VerificationCommandFactory;
import pt.estga.auth.dtos.AuthenticationResponseDto;
import pt.estga.shared.exceptions.EmailAlreadyTakenException;
import pt.estga.shared.exceptions.EmailVerificationRequiredException;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Role;
import pt.estga.user.entities.User;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.service.UserService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceSpringImpl implements AuthenticationService {

    private final UserService userService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationInitiationService verificationInitiationService;
    private final VerificationCommandFactory verificationCommandFactory;
    private final VerificationProcessingService verificationProcessingService;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;
    private final ContactBasedTwoFactorAuthenticationService contactBasedTwoFactorAuthenticationService;
    private final SocialAuthenticationService socialAuthenticationService;

    @Value("${application.security.email.verification-required:false}")
    private boolean emailVerificationRequired;

    @Override
    @Transactional(noRollbackFor = EmailVerificationRequiredException.class)
    public Optional<AuthenticationResponseDto> register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        var email = user.getContacts().stream()
                .filter(c -> c.getType() == ContactType.EMAIL && c.isPrimary())
                .map(UserContact::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Primary email not found"));

        if (userService.existsByEmail(email)) {
            throw new EmailAlreadyTakenException("email already in use");
        }
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        // Default to NONE for 2FA method on registration
        user.setTfaMethod(TfaMethod.NONE);
        user.setEnabled(!emailVerificationRequired);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User createdUser = userService.create(user);

        if (emailVerificationRequired) {
            var command = verificationCommandFactory.createEmailVerificationCommand(createdUser);
            verificationInitiationService.initiate(command);
            throw new EmailVerificationRequiredException("Email verification required. Please check your inbox.");
        }
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

        var refreshTokenString = jwtService.generateRefreshToken(user);
        var accessTokenString = jwtService.generateAccessToken(user);

        var refreshToken = refreshTokenService.createToken(user.getUsername(), refreshTokenString);
        accessTokenService.createToken(user.getUsername(), accessTokenString, refreshToken);

        return Optional.of(new AuthenticationResponseDto(accessTokenString, refreshTokenString, user.getRole().name(), user.getTfaMethod() != TfaMethod.NONE, false, tfaCodeSent));
    }

    @Override
    @Transactional
    public Optional<AuthenticationResponseDto> authenticate(String email, String password, String tfaCode) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", email, e);
            return Optional.empty();
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (emailVerificationRequired && !user.isEnabled()) {
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
                        contactBasedTwoFactorAuthenticationService.generateAndSendSmsCode(user);
                        tfaCodeSent = true;
                        break;
                    case EMAIL:
                        contactBasedTwoFactorAuthenticationService.generateAndSendEmailCode(user);
                        tfaCodeSent = true;
                        break;
                }
                return generateAuthenticationResponse(user, true, tfaCodeSent); // Indicate that 2FA is required
            }

            // A 2FA code was provided, verify it.
            boolean isCodeValid = switch (user.getTfaMethod()) {
                case TOTP -> twoFactorAuthenticationService.isCodeValid(user.getTfaSecret(), tfaCode);
                case SMS ->
                        contactBasedTwoFactorAuthenticationService.verifyCode(user, tfaCode, VerificationTokenPurpose.SMS_2FA);
                case EMAIL ->
                        contactBasedTwoFactorAuthenticationService.verifyCode(user, tfaCode, VerificationTokenPurpose.EMAIL_2FA);
                default -> false;
            };

            if (!isCodeValid) {
                log.warn("Invalid 2FA code for user: {}", email);
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
                .filter(refreshToken -> jwtService.isTokenValid(refreshTokenString, refreshToken.getUser()))
                .map(refreshToken -> {
                    UserDetails userDetails = refreshToken.getUser();

                    accessTokenService.revokeAllByRefreshToken(refreshToken);

                    String newAccessToken = jwtService.generateAccessToken(userDetails);
                    accessTokenService.createToken(userDetails.getUsername(), newAccessToken, refreshToken);

                    return new AuthenticationResponseDto(newAccessToken, refreshTokenString, userDetails.getAuthorities().iterator().next().getAuthority(), ((User)userDetails).getTfaMethod() != TfaMethod.NONE, false, false);
                });
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var command = verificationCommandFactory.createPasswordResetCommand(user);
        verificationInitiationService.initiate(command);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        verificationProcessingService.processPasswordReset(token, newPassword);
    }

    @Override
    public void logoutFromAllDevices(User user) {
        refreshTokenService.revokeAllByUser(user);
        accessTokenService.revokeAllByUser(user);
    }
}
