package pt.estga.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Reauthentication required")
public class ReauthenticationRequiredException extends RuntimeException {
    public ReauthenticationRequiredException(String message) {
        super(message);
    }
}
