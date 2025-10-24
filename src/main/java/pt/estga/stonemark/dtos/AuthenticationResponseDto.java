package pt.estga.stonemark.dtos;

public record AuthenticationResponseDto(
        String token,
        String type,
        String role
) {
    public AuthenticationResponseDto(String token, String role) {
        this(token, "Bearer", role);
    }
}
