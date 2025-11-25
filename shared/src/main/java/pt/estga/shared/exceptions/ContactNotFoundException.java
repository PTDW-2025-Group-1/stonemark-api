package pt.estga.shared.exceptions;

public class ContactNotFoundException extends RuntimeException {
    public ContactNotFoundException(Long id) {
        super("Contact message with ID " + id + " not found");
    }
}
