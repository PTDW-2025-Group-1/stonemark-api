package pt.estga.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class TokenRevokedException extends InvalidTokenException {
    public TokenRevokedException(String message) {
        super(message);
    }
}
