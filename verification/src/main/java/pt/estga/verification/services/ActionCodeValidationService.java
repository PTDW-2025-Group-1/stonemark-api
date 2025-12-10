package pt.estga.verification.services;

import pt.estga.verification.entities.ActionCode;

public interface ActionCodeValidationService {

    ActionCode getValidatedActionCode(String value);
}
