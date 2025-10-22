package pt.estga.stonemark.dtos;

public class AuthenticationResponseDto {
    private String token;

    public AuthenticationResponseDto() {}

    public AuthenticationResponseDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
