package pt.estga.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record TelegramAuthenticationRequestDto(@NotBlank String telegramData) {
}
