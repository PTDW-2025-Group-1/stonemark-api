package pt.estga.stonemark.exceptions;

public class TokenNotFoundException extends RuntimeException {

    public TokenNotFoundException()  {
        super("AccessToken not found");
    }

    public TokenNotFoundException(String token) {
        super("AccessToken not found: " + token);
    }
}
