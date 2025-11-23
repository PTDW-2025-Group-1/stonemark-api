package pt.estga.stonemark.dtos.auth;

public record ResetPasswordRequestDto(String token, String newPassword) {
}
