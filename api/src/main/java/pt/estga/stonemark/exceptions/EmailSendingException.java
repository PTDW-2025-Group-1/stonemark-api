package pt.estga.stonemark.exceptions;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailSendingException(String message) {
        super(message);
    }
}
