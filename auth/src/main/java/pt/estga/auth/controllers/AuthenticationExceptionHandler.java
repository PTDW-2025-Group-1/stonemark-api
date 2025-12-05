package pt.estga.auth.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pt.estga.auth.dtos.ConfirmationResponseDto;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.shared.exceptions.TokenExpiredException;
import pt.estga.shared.exceptions.TokenRevokedException;
import pt.estga.shared.exceptions.InvalidVerificationPurposeException;
import pt.estga.shared.exceptions.SamePasswordException;

@ControllerAdvice(basePackages = "pt.estga.auth")
public class AuthenticationExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ConfirmationResponseDto> handleInvalidTokenException(InvalidTokenException ex) {
        return new ResponseEntity<>(ConfirmationResponseDto.error(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ConfirmationResponseDto> handleTokenExpiredException(TokenExpiredException ex) {
        return new ResponseEntity<>(ConfirmationResponseDto.error(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenRevokedException.class)
    public ResponseEntity<ConfirmationResponseDto> handleTokenRevokedException(TokenRevokedException ex) {
        return new ResponseEntity<>(ConfirmationResponseDto.error(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidVerificationPurposeException.class)
    public ResponseEntity<ConfirmationResponseDto> handleInvalidVerificationPurposeException(InvalidVerificationPurposeException ex) {
        return new ResponseEntity<>(ConfirmationResponseDto.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SamePasswordException.class)
    public ResponseEntity<ConfirmationResponseDto> handleSamePasswordException(SamePasswordException ex) {
        return new ResponseEntity<>(ConfirmationResponseDto.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
