package pt.estga.stonemark.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.config.JwtService;
import pt.estga.stonemark.dtos.AuthenticationRequestDto;
import pt.estga.stonemark.dtos.AuthenticationResponseDto;
import pt.estga.stonemark.dtos.RegisterRequestDto;
import pt.estga.stonemark.entities.User;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceSpringImpl implements AuthenticationService {

    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthenticationResponseDto register(RegisterRequestDto request) {

        User user = userService.save(request.toUser(passwordEncoder));

        var refreshToken = jwtService.generateRefreshToken(user);
        var token = jwtService.generateToken(user);

        tokenService.saveAccessToken(user.getId(), token, refreshToken);
        tokenService.saveRefreshToken(user.getId(), refreshToken);
        return new AuthenticationResponseDto(token, refreshToken);
    }

    @Override
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        var user = userService.findByEmail(request.getEmail()).orElseThrow();
        var refreshToken = jwtService.generateRefreshToken(user);
        var token = jwtService.generateToken(user);

        tokenService.saveAccessToken(user.getId(), token, refreshToken);
        tokenService.saveRefreshToken(user.getId(), refreshToken);
        return new AuthenticationResponseDto(token, refreshToken);
    }

    @Override
    public AuthenticationResponseDto refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return null;
        }
        if (!jwtService.isRefreshToken(refreshToken)) {
            return null;
        }
        final String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null) {
            return null;
        }
        var userOpt = this.userService.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return null;
        }
        var user = userOpt.get();
        if (!jwtService.isTokenValid(refreshToken, user)) {
            return null;
        }
        if (!tokenService.isRefreshTokenValid(refreshToken, user.getId())) {
            return null;
        } // TODO: make this check work

        var accessToken = jwtService.generateToken(user);
        tokenService.revokeAllByRefreshToken(refreshToken);
        tokenService.saveAccessToken(user.getId(), accessToken, refreshToken);
        return AuthenticationResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
