package pt.estga.verification.dtos;

public record ResetPasswordRequestDto(String token, String newPassword) {
}
