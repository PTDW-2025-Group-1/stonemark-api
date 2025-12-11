package pt.estga.shared.exceptions;

public class ContactAlreadyVerifiedException extends RuntimeException {
    public ContactAlreadyVerifiedException(String message) {
        super(message);
    }
}
