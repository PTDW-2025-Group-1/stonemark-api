package pt.estga.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class TokenExpiredException extends InvalidTokenException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
