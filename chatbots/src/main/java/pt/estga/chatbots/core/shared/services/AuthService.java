package pt.estga.chatbots.core.shared.services;

public interface AuthService {
    boolean isAuthenticated(String platformUserId);
    boolean supports(String platform);
}
