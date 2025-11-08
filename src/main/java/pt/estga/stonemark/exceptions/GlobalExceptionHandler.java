package pt.estga.stonemark.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pt.estga.stonemark.dtos.MessageResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SamePasswordException.class)
    public ResponseEntity<MessageResponseDto> handleSamePasswordException(SamePasswordException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponseDto(ex.getMessage()));
    }

    // You can add other global exception handlers here as needed
}
