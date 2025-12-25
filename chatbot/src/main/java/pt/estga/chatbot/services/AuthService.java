package pt.estga.chatbot.services;

public interface AuthService {
    boolean isAuthenticated(String platformUserId);
    boolean supports(String platform);
}
