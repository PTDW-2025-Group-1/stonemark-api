package pt.estga.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SameTelephoneException extends RuntimeException {
    public SameTelephoneException(String message) {
        super(message);
    }
}
