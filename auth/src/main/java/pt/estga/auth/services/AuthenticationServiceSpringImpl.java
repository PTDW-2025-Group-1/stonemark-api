package pt.estga.auth.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
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
import pt.estga.user.entities.UserIdentity;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Provider;
import pt.estga.user.enums.Role;
import pt.estga.user.entities.User;
import pt.estga.user.repositories.UserIdentityRepository;
import pt.estga.user.service.UserService;

import java.io.IOException;
import java.security.GeneralSecurityException;
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
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final VerificationProcessingService verificationProcessingService;
    private final PasswordEncoder passwordEncoder;
    private final UserIdentityRepository userIdentityRepository;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;

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

        user.setEnabled(!emailVerificationRequired);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User createdUser = userService.create(user);

        if (emailVerificationRequired) {
            var command = verificationCommandFactory.createEmailVerificationCommand(createdUser);
            verificationInitiationService.initiate(command);
            throw new EmailVerificationRequiredException("Email verification required. Please check your inbox.");
        }
        return generateAuthenticationResponse(createdUser, false);
    }

    @NotNull
    private Optional<AuthenticationResponseDto> generateAuthenticationResponse(User user, boolean tfaRequired) {
        var refreshTokenString = jwtService.generateRefreshToken(user);
        var accessTokenString = jwtService.generateAccessToken(user);

        var refreshToken = refreshTokenService.createToken(user.getUsername(), refreshTokenString);
        accessTokenService.createToken(user.getUsername(), accessTokenString, refreshToken);

        return Optional.of(new AuthenticationResponseDto(accessTokenString, refreshTokenString, user.getRole().name(), user.isTfaEnabled(), tfaRequired));
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

        if (user.isTfaEnabled()) {
            if (tfaCode == null || tfaCode.isBlank()) {
                return generateAuthenticationResponse(user, true); // Indicate that 2FA is required
            }
            if (!twoFactorAuthenticationService.isCodeValid(user.getTfaSecret(), tfaCode)) {
                log.warn("Invalid 2FA code for user: {}", email);
                return Optional.empty(); // Invalid 2FA code
            }
        }

        return generateAuthenticationResponse(user, false);
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

                    return new AuthenticationResponseDto(newAccessToken, refreshTokenString, userDetails.getAuthorities().iterator().next().getAuthority(), ((User)userDetails).isTfaEnabled(), false);
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
    @Transactional
    public Optional<AuthenticationResponseDto> authenticateWithGoogle(String token) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(token);
            if (idToken == null) {
                return Optional.empty();
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            User user = upsertUserFromGooglePayload(payload);

            if (emailVerificationRequired && !user.isEnabled()) {
                throw new EmailVerificationRequiredException("Email verification required. Please check your inbox.");
            }

            return generateAuthenticationResponse(user, false);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error while authenticating with Google", e);
            throw new RuntimeException("Google authentication failed.", e);
        }
    }

    private User upsertUserFromGooglePayload(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        String googleId = payload.getSubject();

        return userIdentityRepository.findByProviderAndIdentity(Provider.GOOGLE, googleId)
                .map(UserIdentity::getUser)
                .orElseGet(() -> {
                    User user = userService.findByEmail(email)
                            .orElseGet(() -> {
                                User newUser = User.builder()
                                        .username(email)
                                        .firstName((String) payload.get("given_name"))
                                        .lastName((String) payload.get("family_name"))
                                        .role(Role.USER)
                                        .enabled(!emailVerificationRequired)
                                        .build();
                                UserContact primaryEmail = UserContact.builder()
                                        .type(ContactType.EMAIL)
                                        .value(email)
                                        .primary(true)
                                        .verified(true)
                                        .user(newUser)
                                        .build();
                                newUser.setContacts(List.of(primaryEmail));
                                return userService.create(newUser);
                            });

                    UserIdentity identity = UserIdentity.builder()
                            .provider(Provider.GOOGLE)
                            .identity(googleId)
                            .user(user)
                            .build();
                    userIdentityRepository.save(identity);
                    user.getIdentities().add(identity);
                    return userService.update(user);
                });
    }

    @Override
    public void logoutFromAllDevices(User user) {
        refreshTokenService.revokeAllByUser(user);
        accessTokenService.revokeAllByUser(user);
    }

    @Override
    public void logoutFromAllOtherDevices(User user, String currentToken) {
        List<RefreshToken> otherRefreshTokens = refreshTokenService.findAllValidByUser(user);
        otherRefreshTokens.removeIf(token -> token.getToken().equals(currentToken));

        otherRefreshTokens.forEach(accessTokenService::revokeAllByRefreshToken);
        refreshTokenService.revokeAll(otherRefreshTokens);
    }
}
