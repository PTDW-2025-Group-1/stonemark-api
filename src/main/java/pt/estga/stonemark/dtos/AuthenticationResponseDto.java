package pt.estga.stonemark.dtos;

public record AuthenticationResponseDto(
        String token,
        String type,
        String role
) {
    // auxiliar constructor to set default type as "Bearer"
    public AuthenticationResponseDto(String token, String role) {
        this(token, "Bearer", role);
    }
}
