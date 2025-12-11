package pt.estga.auth.dtos;

import lombok.Data;

@Data
public class ReauthenticationRequest {
    private String password;
    private String otp;
}
