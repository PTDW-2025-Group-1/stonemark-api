package pt.estga.stonemark.exceptions;

public class TokenNotFoundException extends RuntimeException {

    public TokenNotFoundException()  {
        super("Token not found");
    }

    public TokenNotFoundException(String token) {
        super("Token not found: " + token);
    }
}
