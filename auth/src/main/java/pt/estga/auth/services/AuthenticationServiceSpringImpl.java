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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.auth.entities.token.RefreshToken;
import pt.estga.auth.services.token.AccessTokenService;
import pt.estga.auth.services.token.RefreshTokenService;
import pt.estga.auth.services.verification.VerificationInitiationService;
import pt.estga.auth.services.verification.VerificationProcessingService;
import pt.estga.auth.services.verification.commands.VerificationCommandFactory;
import pt.estga.auth.dtos.AuthenticationResponseDto;
import pt.estga.shared.exceptions.EmailAlreadyTakenException;
import pt.estga.shared.exceptions.EmailVerificationRequiredException;
import pt.estga.user.Role;
import pt.estga.user.entities.User;
import pt.estga.user.service.UserService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
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

    private final KeycloakAdminService keycloakAdminService;
    private final KeycloakAuthService keycloakAuthService;

    @Value("${application.security.email-verification-required:false}")
    private boolean emailVerificationRequired;

    @Override
    @Transactional(noRollbackFor = EmailVerificationRequiredException.class)
    public Optional<AuthenticationResponseDto> register(User user, String rawPassword) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        var email = user.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be null or blank");
        }
        if (userService.existsByEmail(email)) {
            throw new EmailAlreadyTakenException("email already in use");
        }

        user.setEnabled(!emailVerificationRequired);

        User createdUser = userService.create(user);

        keycloakAdminService.createUserInKeycloak(
                user.getEmail(),
                rawPassword,
                user.getFirstName(),
                user.getLastName()
        );

        if (emailVerificationRequired) {
            var command = verificationCommandFactory.createEmailVerificationCommand(createdUser);
            verificationInitiationService.initiate(command);
            throw new EmailVerificationRequiredException("Email verification required. Please check your inbox.");
        }

        return generateAuthenticationResponse(createdUser);
    }

    @NotNull
    private Optional<AuthenticationResponseDto> generateAuthenticationResponse(User user) {
        var refreshTokenString = jwtService.generateRefreshToken(user);
        var accessTokenString = jwtService.generateAccessToken(user);

        var refreshToken = refreshTokenService.createToken(user.getUsername(), refreshTokenString);
        accessTokenService.createToken(user.getUsername(), accessTokenString, refreshToken);

        return Optional.of(new AuthenticationResponseDto(accessTokenString, refreshTokenString, user.getRole().name()));
    }

    @Override
    @Transactional
    public Optional<AuthenticationResponseDto> authenticate(String email, String password) {

        Map<String, Object> keycloakToken = keycloakAuthService.getUserToken(email, password);

        if (keycloakToken == null || !keycloakToken.containsKey("access_token")) {
            return Optional.empty();
        }

        String accessToken = (String) keycloakToken.get("access_token");

        List<String> kcRoles = keycloakAuthService.extractRoles(accessToken);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (kcRoles.contains("ADMIN")) {
            user.setRole(Role.ADMIN);
        } else if (kcRoles.contains("MODERATOR")) {
            user.setRole(Role.MODERATOR);
        } else {
            user.setRole(Role.USER);
        }

        userService.update(user);

        if (emailVerificationRequired && !user.isEnabled()) {
            throw new EmailVerificationRequiredException("Email verification required.");
        }

        return generateAuthenticationResponse(user);
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

                    return new AuthenticationResponseDto(newAccessToken, refreshTokenString, userDetails.getAuthorities().iterator().next().getAuthority());
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

            return generateAuthenticationResponse(user);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error while authenticating with Google", e);
            throw new RuntimeException("Google authentication failed.", e);
        }
    }

    private User upsertUserFromGooglePayload(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        String googleId = payload.getSubject();

        return userService.findByEmail(email)
                .map(existingUser -> {
                    existingUser.setGoogleId(googleId);
                    return userService.update(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .firstName((String) payload.get("given_name"))
                            .lastName((String) payload.get("family_name"))
                            .role(Role.USER)
                            .enabled(!emailVerificationRequired)
                            .googleId(googleId)
                            .build();
                    return userService.create(newUser);
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
