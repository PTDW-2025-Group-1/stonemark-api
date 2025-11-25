package pt.estga.shared.dtos.auth;

public record ResetPasswordRequestDto(String token, String newPassword) {
}
