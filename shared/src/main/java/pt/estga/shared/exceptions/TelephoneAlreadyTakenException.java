package pt.estga.shared.exceptions;

public class TelephoneAlreadyTakenException extends RuntimeException {
    public TelephoneAlreadyTakenException(String message) {
        super(message);
    }
}
