package pt.estga.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidVerificationPurposeException extends InvalidTokenException {
    public InvalidVerificationPurposeException(String message) {
        super(message);
    }
}
