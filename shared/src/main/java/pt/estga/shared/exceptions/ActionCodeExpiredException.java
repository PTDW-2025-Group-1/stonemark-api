package pt.estga.shared.exceptions;

public class ActionCodeExpiredException extends RuntimeException {
    public ActionCodeExpiredException(String message) {
        super(message);
    }
}
