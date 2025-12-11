package pt.estga.verification.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import pt.estga.verification.enums.ConfirmationStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConfirmationResponseDto(
        ConfirmationStatus status,
        String message,
        String token
) {
    public static ConfirmationResponseDto success(String message) {
        return new ConfirmationResponseDto(ConfirmationStatus.SUCCESS, message, null);
    }

    public static ConfirmationResponseDto passwordResetRequired(String token) {
        return new ConfirmationResponseDto(ConfirmationStatus.PASSWORD_RESET_REQUIRED, "Password reset token issued.", token);
    }

    public static ConfirmationResponseDto error(String message) {
        return new ConfirmationResponseDto(ConfirmationStatus.ERROR, message, null);
    }
}
