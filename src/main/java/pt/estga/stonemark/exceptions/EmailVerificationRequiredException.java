package pt.estga.stonemark.exceptions;

public class EmailVerificationRequiredException extends RuntimeException {
    public EmailVerificationRequiredException(String message) {
        super(message);
    }
}
