package pt.estga.chatbots.core.features.auth;

import pt.estga.user.entities.User;

import java.util.Optional;

public interface AuthService {
    Optional<User> authenticate(String platformUserId, String authenticationData);
    boolean supports(String platform);
}
