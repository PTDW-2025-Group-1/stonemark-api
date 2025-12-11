package pt.estga.user.dtos;

import pt.estga.user.enums.ContactType;

public record ContactDto(String value, ContactType type) { }
