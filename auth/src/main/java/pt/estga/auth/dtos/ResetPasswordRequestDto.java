package pt.estga.auth.dtos;

public record ResetPasswordRequestDto(String token, String newPassword) {
}
