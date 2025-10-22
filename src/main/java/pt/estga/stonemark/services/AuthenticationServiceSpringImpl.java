package pt.estga.stonemark.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.dtos.AuthenticationRequestDto;
import pt.estga.stonemark.dtos.AuthenticationResponseDto;
import pt.estga.stonemark.dtos.RegisterRequestDto;
import pt.estga.stonemark.config.JwtService;
import pt.estga.stonemark.enums.Role;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.respositories.UserRepository;

@Service
public class AuthenticationServiceSpringImpl implements AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationServiceSpringImpl(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthenticationResponseDto register(RegisterRequestDto request) {
        var user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        } else {
            user.setRole(Role.USER);
        }
        repository.save(user);
        var token = jwtService.generateToken(user);
        return new AuthenticationResponseDto(token);
    }

    @Override
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail()).orElseThrow();
        var token = jwtService.generateToken(user);
        return new AuthenticationResponseDto(token);
    }

}
