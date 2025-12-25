package pt.estga.chatbots.services;

public interface AuthService {
    boolean isAuthenticated(String platformUserId);
    boolean supports(String platform);
}
