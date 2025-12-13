package pt.estga.user.dtos;

import pt.estga.user.enums.ContactType;

public record UserContactDto(
        Long id,
        String value,
        ContactType type,
        boolean primaryContact,
        boolean verified
) {}
