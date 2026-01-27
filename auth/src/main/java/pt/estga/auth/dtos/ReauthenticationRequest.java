package pt.estga.auth.dtos;

public record ReauthenticationRequest(
        String password,
        String otp
) { }
