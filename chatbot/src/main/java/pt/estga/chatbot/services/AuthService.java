package pt.estga.chatbot.services;

import pt.estga.chatbot.models.Platform;

public interface AuthService {
    boolean isAuthenticated(String platformUserId);
    boolean supports(Platform platform);
}
